package org.asoem.greyfish.core.simulation;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import jsr166y.ForkJoinPool;
import jsr166y.RecursiveAction;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.*;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.io.ConsoleLogger;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.space.ForwardingSpace2D;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.concurrent.RecursiveActions;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;
import org.asoem.greyfish.utils.space.Object2D;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.*;

/**
 * A {@code Simulation} that uses a {@link ForkJoinPool} to execute {@link Agent}s
 * and process their addition, removal, migration and communication in parallel.
 */
public abstract class Basic2DSimulation<A extends SpatialAgent<A, S, P>, S extends SpatialSimulation2D<A, Z>, Z extends Space2D<A, P>, P extends Object2D> extends Abstract2DSimulation<A, Z> {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(Basic2DSimulation.class);

    private final AgentSpace<Z, A, P> space;
    private final AtomicInteger currentStep = new AtomicInteger(-1);
    private final List<AddAgentMessage<A>> addAgentMessages;
    private final List<RemoveAgentMessage<A>> removeAgentMessages;
    private final List<DeliverAgentMessageMessage<A>> deliverAgentMessageMessages;
    private final KeyedObjectPool<Population, A> agentPool;
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();
    private final ConcurrentMap<String, Object> snapshotValues;
    private final int parallelizationThreshold;
    private final Set<A> prototypes;
    private final SimulationLogger<? super A> simulationLogger;
    private String title = "untitled";
    private final AtomicInteger agentIdSequence = new AtomicInteger();
    private SimulationState state;

    protected Basic2DSimulation(Basic2DSimulationBuilder<?, ?, S, A, Z, P> builder) {
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
    public void addAgent(A agent) {
        checkState(state != SimulationState.PLANING_PHASE);
        checkNotNull(agent, "agent is null");
        // TODO: check state of agent (should be initialized)

        space.insertObject(agent, agent.getProjection());
        agent.activate(ActiveSimulationContext.<S, A>create(self(), agentIdSequence.incrementAndGet(), getStep() + 1));

        LOGGER.debug("Agent activated: {}", agent);

        simulationLogger.logAgentCreation(agent);
    }

    protected abstract S self();

    private void passivateAgentsInternal(List<? extends A> agents) {
        for (A agent : agents) {
            agent.deactivate(PassiveSimulationContext.<S, A>instance());
            releaseAgent(agent);
        }
        space.removeInactiveAgents();
    }

    @Override
    public void removeAgent(final A agent) {
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
    public int countAgents(Population population) {
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
    public Set<A> getPrototypes() {
        return prototypes;
    }

    @Override
    public Z getSpace() {
        return space.delegate();
    }

    @Override
    public double distance(A agent, double degrees) {
        return space.distance(agent, degrees);
    }

    @Override
    public int getStep() {
        return currentStep.get();
    }

    @Override
    public synchronized void nextStep() {
        setState(SimulationState.PLANING_PHASE);

        final int step = currentStep.incrementAndGet();
        LOGGER.info("{}: Entering step {}; {}", this, step, countAgents());

        executeAllAgents();

        setState(SimulationState.MODIFICATION_PHASE);

        processAgentMessageDelivery();
        processRequestedAgentRemovals();
        processAgentsMovement();
        processRequestedAgentActivations();

        afterStepCleanUp();

        setState(SimulationState.IDLE);
    }

    private void afterStepCleanUp() {
        snapshotValues.clear();
    }

    private void processAgentMessageDelivery() {
        for (DeliverAgentMessageMessage<A> message : deliverAgentMessageMessages) {
            for (A agent : message.message.getRecipients()) {
                agent.receive(new AgentMessage<A>(message.message, getStep()));
            }
        }
        deliverAgentMessageMessages.clear();
    }

    private void executeAllAgents() {
        final RecursiveAction executeAllAgents = RecursiveActions.foreach(ImmutableList.copyOf(getAgents()), new VoidFunction<Simulatable<S, A>>() {
            @Override
            public void process(Simulatable<S, A> agent) {
                agent.execute();
            }
        }, parallelizationThreshold);
        forkJoinPool.invoke(executeAllAgents);
    }

    private void processRequestedAgentActivations() {
        for (AddAgentMessage<A> addAgentMessage : addAgentMessages) {
            final A clone = createClone(addAgentMessage.population);
            if (addAgentMessage.chromosome != null)
                clone.updateGeneComponents(addAgentMessage.chromosome);
            addAgentMessage.initializer.initialize(clone);
            addAgent(clone);
        }
        addAgentMessages.clear();
    }

    private void processAgentsMovement() {
        final RecursiveAction moveAllAgents = RecursiveActions.foreach(ImmutableList.copyOf(getAgents()), new VoidFunction<A>() {
            @Override
            public void process(A agent) {
                space.moveObject(agent, agent.getMotion());
            }
        }, parallelizationThreshold);
        forkJoinPool.invoke(moveAllAgents);
    }

    private void processRequestedAgentRemovals() {
        LOGGER.debug("Removing {} agent(s)", removeAgentMessages.size());
        if (removeAgentMessages.size() > 0) {
            passivateAgentsInternal(Lists.transform(removeAgentMessages, new Function<RemoveAgentMessage<A>, A>() {
                @Override
                public A apply(RemoveAgentMessage<A> removeAgentMessage) {
                    return removeAgentMessage.agent;
                }
            }));
            removeAgentMessages.clear();
        }
    }

    @Override
    public void deliverMessage(final ACLMessage<A> message) {
        checkNotNull(message);
        deliverAgentMessageMessages.add(new DeliverAgentMessageMessage<A>(message));
    }

    @Override
    public boolean hasStepValue(String key) {
        return snapshotValues.containsKey(key);
    }

    @Override
    public void setStepValue(String key, Object value) {
        snapshotValues.put(key, value);
    }

    @Override
    public Object getStepValue(String key) {
        return snapshotValues.get(key);
    }

    @Override
    public void createAgent(Population population, Initializer<? super A> initializer) {
        addAgentMessages.add(new AddAgentMessage<A>(population, null, initializer));
    }

    protected void enqueueAgentCreation(Population population, P projection) {
        checkNotNull(population);
        checkNotNull(projection);
        addAgentMessages.add(new AddAgentMessage<A>(population, null, AgentInitializers.projection(projection)));
    }

    protected void enqueueAgentCreation(Population population, Chromosome chromosome, P projection) {
        checkNotNull(population);
        checkNotNull(chromosome);
        checkNotNull(projection);
        addAgentMessages.add(new AddAgentMessage<A>(population, chromosome, AgentInitializers.projection(projection)));
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public void setName(String name) {
        this.title = checkNotNull(name);
    }

    @Override
    public Iterable<A> getAgents(Population population) {
        return space.getAgents(population);
    }

    @Override
    protected SimulationLogger<? super A> getSimulationLogger() {
        return simulationLogger;
    }

    private void setState(SimulationState state) {
        this.state = state;
    }

    public SimulationState getState() {
        return state;
    }

    private static class AddAgentMessage<T> {

        private final Population population;
        private final Initializer<? super T> initializer;
        @Nullable
        private final Chromosome chromosome;

        private AddAgentMessage(Population population, @Nullable Chromosome chromosome, Initializer<? super T> initializer) {
            assert population != null;
            assert initializer != null;
            this.chromosome = chromosome;
            this.population = population;
            this.initializer = initializer;
        }
    }

    private static class RemoveAgentMessage<T> {

        private final T agent;

        public RemoveAgentMessage(T agent) {
            assert agent != null;
            this.agent = agent;
        }

    }

    private static class DeliverAgentMessageMessage<T> {

        private final ACLMessage<T> message;

        public DeliverAgentMessageMessage(ACLMessage<T> message) {
            this.message = message;
        }

    }

    private static class AgentSpace<Z extends Space2D<T, P>, T extends SpatialAgent<?, ?, P>, P extends Object2D> extends ForwardingSpace2D<T, P> {

        private final Z delegate;
        private final Multimap<Population, T> agentsByPopulation;
        private final Predicate<T> INACTIVE_AGENT_PREDICATE = new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                return !input.isActive();
            }
        };

        private AgentSpace(Z delegate) {
            assert delegate != null;

            this.delegate = delegate;
            this.agentsByPopulation = LinkedListMultimap.create();
        }

        @Override
        protected Z delegate() {
            return delegate;
        }

        public int count(Population population) {
            checkNotNull(population);
            return agentsByPopulation.get(population).size();
        }

        @Override
        public boolean insertObject(T object, P projection) {
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
        public boolean removeObject(T agent) {
            checkNotNull(agent);
            if (super.removeObject(agent)) {
                final boolean remove = agentsByPopulation.get(agent.getPopulation()).remove(agent);
                assert remove : "Could not remove " + agent;
                return true;
            }
            return false;
        }

        @Override
        public P getProjection(T object) {
            return checkNotNull(object).getProjection();
        }

        public void removeInactiveAgents() {
            if (super.removeIf(INACTIVE_AGENT_PREDICATE)) {
                Iterables.removeIf(agentsByPopulation.values(), INACTIVE_AGENT_PREDICATE);
            }
        }

        public Iterable<T> getAgents(Population population) {
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

        public Basic2DSimulationBuilder(Z space, final Set<A> prototypes) {
            this.space = checkNotNull(space);
            this.prototypes = checkNotNull(prototypes);
            agentPool(new StackKeyedObjectPool<Population, A>(new BaseKeyedPoolableObjectFactory<Population, A>() {

                Map<Population, A> map = Maps.uniqueIndex(prototypes, new Function<A, Population>() {
                    @Nullable
                    @Override
                    public Population apply(A input) {
                        return input.getPopulation();
                    }
                });

                @SuppressWarnings("unchecked") // casting a clone should be safe
                @Override
                public A makeObject(Population population) throws Exception {
                    return CycleCloner.clone(map.get(population));
                }
            }, 1000));
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            checkState(agentPool != null, "No AgentPool has been defined");
            checkState(!prototypes.contains(null), "Prototypes contains null");
            checkState(space.isEmpty(), "Space is not empty");
        }

        public B agentPool(KeyedObjectPool<Population, A> pool) {
            this.agentPool = checkNotNull(pool);
            return self();
        }

        public B parallelizationThreshold(int parallelizationThreshold) {
            checkArgument(parallelizationThreshold > 0, "parallelizationThreshold must be positive");
            this.parallelizationThreshold = parallelizationThreshold;
            return self();
        }

        public B simulationLogger(SimulationLogger<? super A> simulationLogger) {
            this.simulationLogger = simulationLogger;
            return self();
        }

        public B simulationStepListener(Callback<? super DefaultGreyfishSimulation, ? extends Void> callback) {
            return self();
        }
    }

    private enum SimulationState {
        MODIFICATION_PHASE, IDLE, PLANING_PHASE
    }
}
