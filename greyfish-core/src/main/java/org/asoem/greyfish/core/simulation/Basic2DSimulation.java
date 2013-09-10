package org.asoem.greyfish.core.simulation;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.*;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.*;
import org.asoem.greyfish.core.io.ConsoleLogger;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.space.ForwardingSpace2D;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.core.traits.HeritableTraitsChromosome;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.CycleCloner;
import org.asoem.greyfish.utils.base.InheritableBuilder;
import org.asoem.greyfish.utils.base.Initializer;
import org.asoem.greyfish.utils.space.Object2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.*;

/**
 * A {@code Simulation} that uses a cached thread pool to execute {@link Agent}s
 * and process their addition, removal, migration and communication in parallel.
 */
public abstract class Basic2DSimulation<A extends SpatialAgent<A, S, P>, S extends SpatialSimulation2D<A, Z>,
        Z extends Space2D<A, P>, P extends Object2D> extends Abstract2DSimulation<A, Z> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Basic2DSimulation.class);

    private final AgentSpace<Z, A, P> space;
    private final AtomicInteger currentStep = new AtomicInteger(0);
    private final List<AddAgentMessage<A>> addAgentMessages;
    private final List<RemoveAgentMessage<A>> removeAgentMessages;
    private final List<DeliverAgentMessageMessage<A>> deliverAgentMessageMessages;
    private final KeyedObjectPool<Population, A> agentPool;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ConcurrentMap<String, Object> snapshotValues;
    private final int parallelizationThreshold;
    private final Set<A> prototypes;
    private final SimulationLogger<? super A> simulationLogger;
    private String title = "untitled";
    private final AtomicInteger agentIdSequence = new AtomicInteger();
    private SimulationState state;

    protected Basic2DSimulation(final Basic2DSimulationBuilder<?, ?, S, A, Z, P> builder) {
        this.prototypes = checkNotNull(builder.prototypes);
        this.parallelizationThreshold = builder.parallelizationThreshold;
        this.agentPool = checkNotNull(builder.agentPool);
        this.space = new AgentSpace<Z, A, P>(checkNotNull(builder.space));
        this.simulationLogger = checkNotNull(builder.simulationLogger);

        this.addAgentMessages = checkNotNull(Collections.synchronizedList(Lists.<AddAgentMessage<A>>newArrayList()));
        this.removeAgentMessages = checkNotNull(Collections.synchronizedList(Lists.<RemoveAgentMessage<A>>newArrayList()));
        this.deliverAgentMessageMessages = checkNotNull(Collections.synchronizedList(Lists.<DeliverAgentMessageMessage<A>>newArrayList()));

        this.snapshotValues = Maps.newConcurrentMap();
    }

    @Override
    public final void addAgent(final A agent) {
        checkState(state != SimulationState.PLANING_PHASE);
        checkNotNull(agent, "agent is null");
        // TODO: check state of agent (should be initialized)

        space.insertObject(agent, agent.getProjection());
        agent.activate(ActiveSimulationContext.<S, A>create(self(), agentIdSequence.incrementAndGet(), getSteps()));

        LOGGER.debug("Agent activated: {}", agent);

        simulationLogger.logAgentCreation(agent);
    }

    protected abstract S self();

    private void passivateAgentsInternal(final List<? extends A> agents) {
        for (final A agent : agents) {
            agent.deactivate(PassiveSimulationContext.<S, A>instance());
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
    public final Set<A> getPrototypes() {
        return prototypes;
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
    public final int getSteps() {
        return currentStep.get();
    }

    @Override
    public final synchronized void nextStep() {


        LOGGER.info("{}: Executing step {} with {} active agents", this, getSteps(), countAgents());

        try {
            setState(SimulationState.PLANING_PHASE);

            executeAllAgents();

            setState(SimulationState.MODIFICATION_PHASE);

            processAgentMessageDelivery();
            processRequestedAgentRemovals();
            processAgentsMovement();
            processRequestedAgentActivations();

        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }

        afterStepCleanUp();

        setState(SimulationState.IDLE);

        LOGGER.info("{}: Finished step {}", this, getSteps());

        currentStep.incrementAndGet();
    }

    private void afterStepCleanUp() {
        snapshotValues.clear();
    }

    private void processAgentMessageDelivery() {
        for (final DeliverAgentMessageMessage<A> message : deliverAgentMessageMessages) {
            for (final A agent : message.message.getRecipients()) {
                agent.receive(new AgentMessage<A>(message.message, getSteps()));
            }
        }
        deliverAgentMessageMessages.clear();
    }

    private void executeAllAgents() throws InterruptedException {
        final List<List<A>> partition = Lists.partition(ImmutableList.copyOf(getAgents()), parallelizationThreshold);
        final Collection<Callable<Void>> callables = Lists.transform(partition, new Function<List<A>, Callable<Void>>() {
            @Override
            public Callable<Void> apply(final List<A> input) {
                return new Callable<Void>() {
                    @Override
                    public Void call() {
                        for (A a : input) {
                            a.execute();
                        }
                        return null;
                    }
                };
            }
        });

        executorService.invokeAll(callables);
    }

    private void processRequestedAgentActivations() {
        for (final AddAgentMessage<A> addAgentMessage : addAgentMessages) {
            final A clone = createClone(addAgentMessage.population);
            final Chromosome chromosome = Optional
                    .fromNullable(addAgentMessage.chromosome)
                    .or(HeritableTraitsChromosome.initializeFromAgent(clone));
            chromosome.updateAgent(clone);
            addAgentMessage.initializer.initialize(clone);
            addAgent(clone);
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

    @Override
    public final boolean hasStepValue(final String key) {
        return snapshotValues.containsKey(key);
    }

    @Override
    public final void setStepValue(final String key, final Object value) {
        snapshotValues.put(key, value);
    }

    @Override
    public final Object getStepValue(final String key) {
        return snapshotValues.get(key);
    }

    @Override
    public final void createAgent(final Population population, final Initializer<? super A> initializer) {
        addAgentMessages.add(new AddAgentMessage<A>(population, null, initializer));
    }

    protected final void enqueueAgentCreation(final Population population, final P projection) {
        checkNotNull(population);
        checkNotNull(projection);
        addAgentMessages.add(new AddAgentMessage<A>(population, null, AgentInitializers.projection(projection)));
    }

    protected final void enqueueAgentCreation(final Population population, final Chromosome chromosome, final P projection) {
        checkNotNull(population);
        checkNotNull(chromosome);
        checkNotNull(projection);
        addAgentMessages.add(new AddAgentMessage<A>(population, chromosome, AgentInitializers.projection(projection)));
    }

    @Override
    public final String getName() {
        return title;
    }

    @Override
    public final void setName(final String name) {
        this.title = checkNotNull(name);
    }

    @Override
    public final Iterable<A> getAgents(final Population population) {
        return space.getAgents(population);
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

    private static class AddAgentMessage<T> {

        private final Population population;
        private final Initializer<? super T> initializer;
        @Nullable
        private final Chromosome chromosome;

        private AddAgentMessage(final Population population, @Nullable final Chromosome chromosome,
                                final Initializer<? super T> initializer) {
            assert population != null;
            assert initializer != null;
            this.chromosome = chromosome;
            this.population = population;
            this.initializer = initializer;
        }
    }

    private static class RemoveAgentMessage<T> {

        private final T agent;

        public RemoveAgentMessage(final T agent) {
            assert agent != null;
            this.agent = agent;
        }

    }

    private static class DeliverAgentMessageMessage<T> {

        private final ACLMessage<T> message;

        public DeliverAgentMessageMessage(final ACLMessage<T> message) {
            this.message = message;
        }

    }

    private static class AgentSpace<Z extends Space2D<T, P>, T extends SpatialAgent<?, ?, P>, P extends Object2D> extends ForwardingSpace2D<T, P> {

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

    protected abstract static class Basic2DSimulationBuilder<B extends Basic2DSimulationBuilder<B, S, X, A, Z, P>, S extends Basic2DSimulation<A, X, Z, P>, X extends SpatialSimulation2D<A, Z>, A extends SpatialAgent<A, X, P>, Z extends Space2D<A, P>, P extends Object2D> extends InheritableBuilder<S, B> {

        private KeyedObjectPool<Population, A> agentPool;
        private int parallelizationThreshold = 1000;
        private final Z space;
        private final Set<A> prototypes;
        private SimulationLogger<? super A> simulationLogger = new ConsoleLogger<A>();

        public Basic2DSimulationBuilder(final Z space, final Set<A> prototypes) {
            this.space = checkNotNull(space);
            this.prototypes = checkNotNull(prototypes);
            agentPool(new StackKeyedObjectPool<Population, A>(new BaseKeyedPoolableObjectFactory<Population, A>() {

                Map<Population, A> map = Maps.uniqueIndex(prototypes, new Function<A, Population>() {
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
        }

        public final B agentPool(final KeyedObjectPool<Population, A> pool) {
            this.agentPool = checkNotNull(pool);
            return self();
        }

        public final B parallelizationThreshold(final int parallelizationThreshold) {
            checkArgument(parallelizationThreshold > 0, "parallelizationThreshold must be positive");
            this.parallelizationThreshold = parallelizationThreshold;
            return self();
        }

        public final B simulationLogger(final SimulationLogger<? super A> simulationLogger) {
            this.simulationLogger = simulationLogger;
            return self();
        }

        public final B simulationStepListener(final Callback<? super DefaultGreyfishSimulation, ? extends Void> callback) {
            return self();
        }
    }

    private enum SimulationState {
        MODIFICATION_PHASE, IDLE, PLANING_PHASE
    }
}
