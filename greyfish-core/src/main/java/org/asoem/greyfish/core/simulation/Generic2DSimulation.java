package org.asoem.greyfish.core.simulation;

import com.google.common.base.*;
import com.google.common.collect.*;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.*;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.io.SimulationLoggers;
import org.asoem.greyfish.core.space.ForwardingSpace2D;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.core.traits.HeritableTraitsChromosome;
import org.asoem.greyfish.core.utils.DiscreteTimeListener;
import org.asoem.greyfish.utils.base.CycleCloner;
import org.asoem.greyfish.utils.base.InheritableBuilder;
import org.asoem.greyfish.utils.base.Initializer;
import org.asoem.greyfish.utils.space.Object2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.*;

/**
 * A {@code Simulation} that uses a cached thread pool to execute {@link Agent}s
 * and process their addition, removal, migration and communication in parallel.
 */
public abstract class Generic2DSimulation<A extends SpatialAgent<A, S, P>, S extends SpatialSimulation2D<A, Z>,
        Z extends Space2D<A, P>, P extends Object2D> extends Abstract2DSimulation<A, Z> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Generic2DSimulation.class);

    private final AgentSpace<Z, A, P> space;
    private final AtomicInteger currentStep = new AtomicInteger(0);
    private final List<NewAgentEvent<P, A>> addAgentMessages;
    private final List<RemoveAgentMessage<A>> removeAgentMessages;
    private final List<DeliverAgentMessageMessage<A>> deliverAgentMessageMessages;
    private final KeyedObjectPool<Population, A> agentPool;
    private final ExecutorService executorService;
    private final ConcurrentMap<String, Object> snapshotValues;
    private final int parallelizationThreshold;
    private final Set<A> prototypes;
    private final SimulationLogger<? super A> simulationLogger;
    private String title = "untitled";
    private final AtomicInteger agentIdSequence = new AtomicInteger();
    private SimulationState state;

    protected Generic2DSimulation(final Basic2DSimulationBuilder<?, ?, S, A, Z, P> builder) {
        this.prototypes = checkNotNull(builder.prototypes);
        this.parallelizationThreshold = builder.parallelizationThreshold;
        this.agentPool = checkNotNull(builder.agentPool);
        this.space = new AgentSpace<Z, A, P>(checkNotNull(builder.space));
        this.simulationLogger = checkNotNull(builder.simulationLogger);

        this.addAgentMessages = checkNotNull(Collections.synchronizedList(Lists.<NewAgentEvent<P, A>>newArrayList()));
        this.removeAgentMessages = checkNotNull(Collections.synchronizedList(Lists.<RemoveAgentMessage<A>>newArrayList()));
        this.deliverAgentMessageMessages = checkNotNull(Collections.synchronizedList(Lists.<DeliverAgentMessageMessage<A>>newArrayList()));

        this.snapshotValues = Maps.newConcurrentMap();
        executorService = builder.executionService;
    }

    /**
     * Add an agent to this simulation at given {@code projection} in space.
     * @param agent the agent insert
     * @param projection the projection
     */
    public final void addAgent(final A agent, final P projection) {
        addAgentMessages.add(new InjectedAgentEvent(agent, projection));
    }

    private void insertAgent(final A agent, final P projection) {
        checkState(state != SimulationState.PLANING_PHASE);
        checkNotNull(agent, "agent is null");
        // TODO: check state of agent (should be initialized)

        space.insertObject(agent, projection);
        agent.activate(DefaultActiveSimulationContext.<S, A>create(self(), agentIdSequence.incrementAndGet(), getTime()));

        LOGGER.debug("Agent activated: {}", agent);

        simulationLogger.logAgentCreation(agent);
    }

    protected abstract S self();

    private void passivateAgentsInternal(final List<? extends A> agents) {
        for (final A agent : agents) {
            agent.deactivate(SimulationContexts.<S, A>instance());
            releaseAgent(agent);
        }
        space.removeInactiveAgents();
    }

    @Override
    public final void removeAgent(final A agent) {
        checkNotNull(agent);
        removeAgentMessages.add(new RemoveAgentMessage<A>(agent));
    }

    private void releaseAgent(final A agent) {
        try {
            agentPool.returnObject(agent.getPopulation(), agent);
        } catch (Exception e) {
            LOGGER.error("Error in prototype pool", e);
        }
    }

    @Override
    public final int countAgents(final Population population) {
        return space.count(population);
    }

    private A createClone(final Population population) {
        checkNotNull(population);
        try {
            final A agent = agentPool.borrowObject(population);
            checkNotNull(agent, "borrowObject in agentPool returned null");
            agent.initialize();
            return agent;
        } catch (Exception e) {
            LOGGER.error("Couldn't borrow Agent from agentPool for population {}", population.getName(), e);
            throw new AssertionError(e);
        }
    }

    @Override
    public final Z getSpace() {
        return space.delegate();
    }

    @Override
    public final double distance(final A agent, final double degrees) {
        return space.distance(agent, degrees);
    }

    @Override
    public final long getTime() {
        return currentStep.get();
    }

    @Override
    public final synchronized void nextStep() {


        LOGGER.debug("{}: Executing step {} with {} active agents", this, getTime(), countAgents());

        try {
            setState(SimulationState.PLANING_PHASE);

            executeAllAgents();

            setState(SimulationState.MODIFICATION_PHASE);

            processAgentMessageDelivery();
            processRequestedAgentRemovals();
            processAgentsMovement();
            processRequestedAgentActivations();

        } catch (Throwable e) {
            throw Throwables.propagate(e);
        }

        afterStepCleanUp();

        setState(SimulationState.IDLE);

        LOGGER.debug("{}: Finished step {}", this, getTime());

        currentStep.incrementAndGet();
    }

    private void afterStepCleanUp() {
        snapshotValues.clear();
    }

    private void processAgentMessageDelivery() {
        for (final DeliverAgentMessageMessage<A> message : deliverAgentMessageMessages) {
            for (final A agent : message.message.getRecipients()) {
                agent.receive(message.message);
            }
        }
        deliverAgentMessageMessages.clear();
    }

    private void executeAllAgents() throws InterruptedException, ExecutionException {
        final List<List<A>> partition = Lists.partition(ImmutableList.copyOf(getAgents()), parallelizationThreshold);
        final Collection<Callable<Void>> callables = Lists.transform(partition, new Function<List<A>, Callable<Void>>() {
            @Override
            public Callable<Void> apply(final List<A> input) {
                return new Callable<Void>() {
                    @Override
                    public Void call() {
                        for (A a : input) {
                            a.run();
                        }
                        return null;
                    }
                };
            }
        });

        final List<Future<Void>> futures = executorService.invokeAll(callables);
        for (Future<Void> future : futures) {
            future.get();
        }
    }

    private void processRequestedAgentActivations() {
        for (final NewAgentEvent<P, A> addAgentMessage : addAgentMessages) {
            final A clone = addAgentMessage.getAgent();
            insertAgent(clone, addAgentMessage.getProjection());
        }
        addAgentMessages.clear();
    }

    private void processAgentsMovement() throws InterruptedException {
        final List<List<A>> partition = Lists.partition(ImmutableList.copyOf(getAgents()), parallelizationThreshold);
        final Collection<Callable<Void>> callables = Lists.transform(partition, new Function<List<A>, Callable<Void>>() {
            @Override
            public Callable<Void> apply(final List<A> input) {
                return new Callable<Void>() {
                    @Override
                    public Void call() {
                        for (A a : input) {
                            space.moveObject(a, a.getMotion());
                        }
                        return null;
                    }
                };
            }
        });

        executorService.invokeAll(callables);
    }

    /**
     * Remove all agents from this simulation and the underlying {@code #space} as requested by {@link #removeAgentMessages}
     */
    private void processRequestedAgentRemovals() {
        LOGGER.debug("Removing {} agent(s)", removeAgentMessages.size());
        if (removeAgentMessages.size() > 0) {
            passivateAgentsInternal(Lists.transform(removeAgentMessages, new Function<RemoveAgentMessage<A>, A>() {
                @Override
                public A apply(final RemoveAgentMessage<A> removeAgentMessage) {
                    return removeAgentMessage.agent;
                }
            }));
            removeAgentMessages.clear();
        }
    }

    @Override
    public final void deliverMessage(final ACLMessage<A> message) {
        checkNotNull(message);
        deliverAgentMessageMessages.add(new DeliverAgentMessageMessage<A>(message));
    }

    protected final void enqueueAgentCreation(final Population population, final P projection) {
        checkNotNull(population);
        checkNotNull(projection);
        addAgentMessages.add(new CreateCloneMessage(population, null, AgentInitializers.projection(projection)));
    }

    protected final void enqueueAgentCreation(final Population population, final Chromosome chromosome, final P projection) {
        checkNotNull(population);
        checkNotNull(chromosome);
        checkNotNull(projection);
        addAgentMessages.add(new CreateCloneMessage(population, chromosome, AgentInitializers.projection(projection)));
    }

    @Override
    public final String getName() {
        return title;
    }

    @Override
    public final Iterable<A> getAgents(final Population population) {
        return space.getAgents(population);
    }

    @Override
    public final int numberOfPopulations() {
        return space.agentsByPopulation.size();
    }

    @Override
    protected final SimulationLogger<? super A> getSimulationLogger() {
        return simulationLogger;
    }

    private void setState(final SimulationState state) {
        LOGGER.debug("Switching state: {} -> {}", this.state, state);
        this.state = state;
    }

    public final SimulationState getState() {
        return state;
    }

    @Override
    public final void addTimeChangeListener(final DiscreteTimeListener timeListener) {
        throw new UnsupportedOperationException("Not yet implemented"); // TODO implement
    }

    private interface NewAgentEvent<P extends Object2D, A extends SpatialAgent<A, ?, P>> {
        A getAgent();

        P getProjection();
    }

    private class CreateCloneMessage implements NewAgentEvent<P, A> {

        private final Population population;
        private final Initializer<? super A> initializer;
        @Nullable
        private final Chromosome chromosome;

        private final Supplier<A> supplier = Suppliers.memoize(new Supplier<A>() {
            @Override
            public A get() {
                final A clone = createClone(population);
                Optional.fromNullable(chromosome)
                        .or(HeritableTraitsChromosome.initializeFromAgent(clone))
                        .updateAgent(clone);
                initializer.initialize(clone);
                return clone;
            }
        });

        private CreateCloneMessage(final Population population, @Nullable final Chromosome chromosome,
                                   final Initializer<? super A> initializer) {
            assert population != null;
            assert initializer != null;
            this.chromosome = chromosome;
            this.population = population;
            this.initializer = initializer;
        }

        @Override
        public A getAgent() {
           return supplier.get();
        }

        @Override
        public P getProjection() {
            return getAgent().getProjection();
        }
    }

    private class InjectedAgentEvent implements NewAgentEvent<P, A> {

        private final A agent;
        private final P projection;

        private InjectedAgentEvent(final A agent, final P projection) {
            this.projection = checkNotNull(projection);
            this.agent = checkNotNull(agent);
        }

        @Override
        public A getAgent() {
            return agent;
        }

        @Override
        public P getProjection() {
            return projection;
        }
    }

    private static class RemoveAgentMessage<A> {

        private final A agent;

        public RemoveAgentMessage(final A agent) {
            assert agent != null;
            this.agent = agent;
        }

    }

    private static class DeliverAgentMessageMessage<A> {

        private final ACLMessage<A> message;

        public DeliverAgentMessageMessage(final ACLMessage<A> message) {
            this.message = message;
        }

    }

    private static final class AgentSpace<Z extends Space2D<T, P>, T extends SpatialAgent<?, ?, P>, P extends Object2D>
            extends ForwardingSpace2D<T, P> {

        private final Z delegate;
        private final Multimap<Population, T> agentsByPopulation;
        private final Predicate<T> inactiveAgentPredicate = new Predicate<T>() {
            @Override
            public boolean apply(final T input) {
                return !input.isActive();
            }
        };

        private AgentSpace(final Z delegate) {
            assert delegate != null;

            this.delegate = delegate;
            this.agentsByPopulation = LinkedListMultimap.create();
        }

        @Override
        protected Z delegate() {
            return delegate;
        }

        public int count(final Population population) {
            checkNotNull(population);
            return agentsByPopulation.get(population).size();
        }

        @Override
        public boolean insertObject(final T object, final P projection) {
            checkNotNull(object, "projectable is null");
            checkNotNull(projection, "projection is null");

            if (super.insertObject(object, projection)) {
                final boolean add = agentsByPopulation.get(object.getPopulation()).add(object);
                object.setProjection(projection);
                assert add : "Could not add " + object;
                return true;
            }
            return false;
        }

        @Override
        public boolean removeObject(final T agent) {
            checkNotNull(agent);
            if (super.removeObject(agent)) {
                final boolean remove = agentsByPopulation.get(agent.getPopulation()).remove(agent);
                agent.setProjection(null);
                assert remove : "Could not remove " + agent;
                return true;
            }
            return false;
        }

        @Override
        public P getProjection(final T object) {
            return checkNotNull(object).getProjection();
        }

        public void removeInactiveAgents() {
            if (super.removeIf(inactiveAgentPredicate)) {
                Iterables.removeIf(agentsByPopulation.values(), inactiveAgentPredicate);
            }
        }

        public Iterable<T> getAgents(final Population population) {
            checkNotNull(population);
            return agentsByPopulation.get(population);
        }
    }

    protected abstract static class Basic2DSimulationBuilder<B extends Basic2DSimulationBuilder<B, S, X, A, Z, P>, S extends Generic2DSimulation<A, X, Z, P>, X extends SpatialSimulation2D<A, Z>, A extends SpatialAgent<A, X, P>, Z extends Space2D<A, P>, P extends Object2D> extends InheritableBuilder<S, B> {

        private KeyedObjectPool<Population, A> agentPool;
        private int parallelizationThreshold = 1000;
        private final Z space;
        private final Set<A> prototypes;
        private SimulationLogger<? super A> simulationLogger = SimulationLoggers.<A>consoleLogger();
        private ExecutorService executionService = Executors.newCachedThreadPool();

        public Basic2DSimulationBuilder(final Z space, final Set<A> prototypes) {
            this.space = checkNotNull(space);
            this.prototypes = checkNotNull(prototypes);
            agentPool(new StackKeyedObjectPool<Population, A>(new BaseKeyedPoolableObjectFactory<Population, A>() {

                private final Map<Population, A> map = Maps.uniqueIndex(prototypes, new Function<A, Population>() {
                    @Nullable
                    @Override
                    public Population apply(final A input) {
                        return input.getPopulation();
                    }
                });

                @SuppressWarnings("unchecked") // casting a clone should be safe
                @Override
                public A makeObject(final Population population) throws Exception {
                    return CycleCloner.clone(map.get(population));
                }
            }, 1000));
        }

        @Override
        protected void checkBuilder() {
            checkState(agentPool != null, "No AgentPool has been defined");
            checkState(!prototypes.contains(null), "Prototypes contains null");
            checkState(space.isEmpty(), "Space is not empty");
            checkState(executionService != null, "The execution service must not be null");
        }

        /**
         * Set the agent pool to use for recycling objects of tye {@code A}.
         * @param pool the pool to use for recycling
         * @return this builder
         */
        public final B agentPool(final KeyedObjectPool<Population, A> pool) {
            this.agentPool = checkNotNull(pool);
            return self();
        }

        /**
         * Set the parallelization threshold after above which to parallelize agent executions.
         * @param parallelizationThreshold the threshold for parallelling agent executions
         * @return this builder
         */
        public final B parallelizationThreshold(final int parallelizationThreshold) {
            checkArgument(parallelizationThreshold > 0, "parallelizationThreshold must be positive");
            this.parallelizationThreshold = parallelizationThreshold;
            return self();
        }

        /**
         * Set the simulation logger to use for logging simulation events.
         * @param simulationLogger the simulation logger
         * @return this builder
         */
        public final B simulationLogger(final SimulationLogger<? super A> simulationLogger) {
            this.simulationLogger = checkNotNull(simulationLogger);
            return self();
        }

        /**
         * Set the executor service used to execute agents.
         * @see org.asoem.greyfish.core.agent.Agent#run()
         * @param executionService the execution servive to use
         * @return this builder
         */
        public final B executionService(final ExecutorService executionService) {
            this.executionService = checkNotNull(executionService);
            return self();
        }
    }

    private enum SimulationState {
        MODIFICATION_PHASE, IDLE, PLANING_PHASE
    }
}
