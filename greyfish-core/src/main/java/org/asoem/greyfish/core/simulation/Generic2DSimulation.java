package org.asoem.greyfish.core.simulation;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.*;
import com.google.common.eventbus.EventBus;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.PrototypeGroup;
import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.core.space.ForwardingSpace2D;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.impl.simulation.AgentAddedEvent;
import org.asoem.greyfish.utils.base.InheritableBuilder;
import org.asoem.greyfish.utils.collect.LoadingKeyedObjectPool;
import org.asoem.greyfish.utils.space.Object2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.*;

/**
 * A {@code Simulation} that uses a cached thread pool to execute {@link Agent}s and process their addition, removal,
 * migration and communication in parallel.
 */
public abstract class Generic2DSimulation<A extends SpatialAgent<A, ?, P, ?>, S extends SpatialSimulation2D<A, Z>,
        Z extends Space2D<A, P>, P extends Object2D> extends Abstract2DSimulation<A, Z> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Generic2DSimulation.class);

    private final AgentSpace<Z, A, P> space;
    private final AtomicInteger currentStep = new AtomicInteger(0);
    private final List<NewAgentEvent<P, A>> addAgentMessages;
    private final List<RemoveAgentMessage<A>> removeAgentMessages;
    private final List<DeliverAgentMessageMessage<A>> deliverAgentMessageMessages;
    private final LoadingKeyedObjectPool<PrototypeGroup, A> agentPool;
    private final ExecutorService executorService;
    private final ConcurrentMap<String, Object> snapshotValues;
    private final int parallelizationThreshold;
    private final Set<A> prototypes;
    private final EventBus eventBus;
    private String title = "untitled";
    private SimulationState state;

    protected Generic2DSimulation(final Basic2DSimulationBuilder<?, ?, S, A, Z, P> builder) {
        this.prototypes = checkNotNull(builder.prototypes);
        this.parallelizationThreshold = builder.parallelizationThreshold;
        this.agentPool = checkNotNull(builder.agentPool);
        this.space = new AgentSpace<>(checkNotNull(builder.space));

        this.addAgentMessages = checkNotNull(Collections.synchronizedList(Lists.<NewAgentEvent<P, A>>newArrayList()));
        this.removeAgentMessages = checkNotNull(Collections.synchronizedList(Lists.<RemoveAgentMessage<A>>newArrayList()));
        this.deliverAgentMessageMessages = checkNotNull(Collections.synchronizedList(Lists.<DeliverAgentMessageMessage<A>>newArrayList()));

        this.snapshotValues = Maps.newConcurrentMap();
        this.executorService = builder.executionService;
        this.eventBus = builder.eventPublisher;
    }

    /**
     * Add an agent to this getSimulation at given {@code projection} in space.
     *
     * @param agent      the agent insert
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
        activateAgent(agent);

        LOGGER.debug("Agent activated: {}", agent);

        eventBus.post(new AgentAddedEvent(agent, this));
    }

    protected abstract void activateAgent(final A agent);

    protected abstract S self();

    private void passivateAgentsInternal(final List<? extends A> agents) {
        for (final A agent : agents) {
            agent.deactivate();
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
            agentPool.release(agent.getPrototypeGroup(), agent);
        } catch (Exception e) {
            LOGGER.error("Error in prototype pool", e);
        }
    }

    @Override
    public final int countAgents(final PrototypeGroup prototypeGroup) {
        return space.count(prototypeGroup);
    }

    private A createClone(final PrototypeGroup prototypeGroup) {
        checkNotNull(prototypeGroup);
        try {
            final A agent = agentPool.borrow(prototypeGroup);
            checkNotNull(agent, "borrowObject in agentPool returned null");
            agent.initialize();
            return agent;
        } catch (Exception e) {
            LOGGER.error("Couldn't borrow Agent from agentPool for prototypeGroup {}", prototypeGroup.getName(), e);
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
                agent.ask(message.message, Void.class);
            }
        }
        deliverAgentMessageMessages.clear();
    }

    private void executeAllAgents() throws InterruptedException, ExecutionException {
        final List<List<A>> partition = Lists.partition(ImmutableList.copyOf(getActiveAgents()), parallelizationThreshold);
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
        final List<List<A>> partition = Lists.partition(ImmutableList.copyOf(getActiveAgents()), parallelizationThreshold);
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
     * Remove all agents from this getSimulation and the underlying {@code #space} as requested by {@link
     * #removeAgentMessages}
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

    protected final void enqueueAgentCreation(final A population, final P projection) {
        checkNotNull(population);
        checkNotNull(projection);
        addAgentMessages.add(new AddAgentMessage(population, projection));
    }

    @Override
    public final String getName() {
        return title;
    }

    @Override
    public final Iterable<A> getAgents(final PrototypeGroup prototypeGroup) {
        return space.getAgents(prototypeGroup);
    }

    @Override
    public final int numberOfPopulations() {
        return space.agentsByPopulation.size();
    }

    private void setState(final SimulationState state) {
        LOGGER.debug("Switching state: {} -> {}", this.state, state);
        this.state = state;
    }

    public final SimulationState getState() {
        return state;
    }

    @Override
    public final String getStatusInfo() {
        return String.format("%d agents; %d steps", countAgents(), getTime());
    }

    private interface NewAgentEvent<P extends Object2D, A extends SpatialAgent<A, ?, P, ?>> {
        A getAgent();

        P getProjection();
    }

    private class AddAgentMessage implements NewAgentEvent<P, A> {

        private final A agent;
        private final P projection;

        public AddAgentMessage(final A agent, final P projection) {
            this.agent = checkNotNull(agent);
            this.projection = checkNotNull(projection);
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

    private static final class AgentSpace<Z extends Space2D<T, P>, T extends SpatialAgent<?, ?, P, ?>, P extends Object2D>
            extends ForwardingSpace2D<T, P> {

        private final Z delegate;
        private final Multimap<PrototypeGroup, T> agentsByPopulation;
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

        public int count(final PrototypeGroup prototypeGroup) {
            checkNotNull(prototypeGroup);
            return agentsByPopulation.get(prototypeGroup).size();
        }

        @Override
        public boolean insertObject(final T object, final P projection) {
            checkNotNull(object, "projectable is null");
            checkNotNull(projection, "projection is null");

            if (super.insertObject(object, projection)) {
                final boolean add = agentsByPopulation.get(object.getPrototypeGroup()).add(object);
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
                final boolean remove = agentsByPopulation.get(agent.getPrototypeGroup()).remove(agent);
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

        public Iterable<T> getAgents(final PrototypeGroup prototypeGroup) {
            checkNotNull(prototypeGroup);
            return agentsByPopulation.get(prototypeGroup);
        }
    }

    protected abstract static class Basic2DSimulationBuilder<B extends Basic2DSimulationBuilder<B, S, X, A, Z, P>, S extends Generic2DSimulation<A, X, Z, P>, X extends SpatialSimulation2D<A, Z>, A extends SpatialAgent<A, ?, P, ?>, Z extends Space2D<A, P>, P extends Object2D> extends InheritableBuilder<S, B> {

        private LoadingKeyedObjectPool<PrototypeGroup, A> agentPool;
        private int parallelizationThreshold = 1000;
        private final Z space;
        private final Set<A> prototypes;
        private ExecutorService executionService = Executors.newCachedThreadPool();
        private EventBus eventPublisher = new EventBus();

        public Basic2DSimulationBuilder(final Z space, final Set<A> prototypes) {
            this.space = checkNotNull(space);
            this.prototypes = checkNotNull(prototypes);
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
         *
         * @param pool the pool to use for recycling
         * @return this builder
         */
        public final B agentPool(final LoadingKeyedObjectPool<PrototypeGroup, A> pool) {
            this.agentPool = checkNotNull(pool);
            return self();
        }

        /**
         * Set the parallelization threshold after above which to parallelize agent executions.
         *
         * @param parallelizationThreshold the threshold for parallelling agent executions
         * @return this builder
         */
        public final B parallelizationThreshold(final int parallelizationThreshold) {
            checkArgument(parallelizationThreshold > 0, "parallelizationThreshold must be positive");
            this.parallelizationThreshold = parallelizationThreshold;
            return self();
        }

        /**
         * Set the executor service used to execute agents.
         *
         * @param executionService the execution servive to use
         * @return this builder
         * @see org.asoem.greyfish.core.agent.Agent#run()
         */
        public final B executionService(final ExecutorService executionService) {
            this.executionService = checkNotNull(executionService);
            return self();
        }

        public final B eventBus(final EventBus eventBus) {
            this.eventPublisher = checkNotNull(eventBus);
            return self();
        }
    }

    private enum SimulationState {
        MODIFICATION_PHASE, IDLE, PLANING_PHASE
    }
}
