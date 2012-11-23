package org.asoem.greyfish.core.simulation;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.*;
import jsr166y.ForkJoinPool;
import jsr166y.RecursiveAction;
import org.apache.commons.pool.KeyedObjectPool;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.*;
import org.asoem.greyfish.core.io.ConsoleLogger;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.space.ForwardingSpace2D;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.base.Builder;
import org.asoem.greyfish.utils.base.Initializer;
import org.asoem.greyfish.utils.base.VoidFunction;
import org.asoem.greyfish.utils.concurrent.RecursiveActions;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;
import org.asoem.greyfish.utils.space.Object2D;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.*;

/**
 * A {@code Simulation} that uses a {@link ForkJoinPool} to execute {@link Agent}s
 * and process their addition, removal, migration and communication in parallel.
 */
public abstract class ParallelizedSimulation<A extends Agent<A, S, P>, S extends SpatialSimulation<A, Z>, Z extends Space2D<A, P>, P extends Object2D> extends AbstractSpatialSimulation<A, Z> {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(ParallelizedSimulation.class);

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
    private final SimulationLogger simulationLogger;
    private final AtomicInteger agentIdSequence = new AtomicInteger();
    private String title = "untitled";

    private ParallelizedSimulation(ParallelizedSimulationBuilder<A, S, Z, P> builder) {
        this(builder.space,
                builder.prototypes,
                builder.parallelizationThreshold,
                builder.simulationLogger,
                builder.agentPool,
                Collections.synchronizedList(Lists.<AddAgentMessage<A>>newArrayList()),
                Collections.synchronizedList(Lists.<RemoveAgentMessage<A>>newArrayList()),
                Collections.synchronizedList(Lists.<DeliverAgentMessageMessage<A>>newArrayList()));
    }

    protected ParallelizedSimulation(
                                  @Element(name = "space") Z space,
                                  @ElementList(name = "prototypes") Set<A> prototypes,
                                  @Attribute(name = "parallelizationThreshold") int parallelizationThreshold,
                                  @Element(name = "simulationLogger") SimulationLogger simulationLogger,
                                  @Element(name = "agentPool") KeyedObjectPool<Population, A> agentPool,
                                  @ElementList(name = "addAgentMessages", required = false, empty = false, entry = "addAgentMessage", inline = true) List<AddAgentMessage<A>> addAgentMessages,
                                  @ElementList(name = "removeAgentMessages", required = false, empty = false, entry = "removeAgentMessage", inline = true) List<RemoveAgentMessage<A>> removeAgentMessages,
                                  @ElementList(name = "deliverAgentMessageMessages", required = false, empty = false, entry = "deliverAgentMessageMessage", inline = true) List<DeliverAgentMessageMessage<A>> deliverAgentMessageMessages) {

        this.prototypes = prototypes;
        this.parallelizationThreshold = parallelizationThreshold;
        this.agentPool = agentPool;
        this.space = new AgentSpace<Z, A, P>(space);
        this.simulationLogger = simulationLogger;

        this.addAgentMessages = addAgentMessages;
        this.removeAgentMessages = removeAgentMessages;
        this.deliverAgentMessageMessages = deliverAgentMessageMessages;

        this.snapshotValues = Maps.newConcurrentMap();
    }

    protected ParallelizedSimulation(Z space, Set<A> prototypes, int parallelizationThreshold, SimulationLogger simulationLogger, KeyedObjectPool<Population, A> agentPool) {
        this(space,
                prototypes,
                parallelizationThreshold,
                simulationLogger,
                agentPool,
                Collections.synchronizedList(Lists.<AddAgentMessage<A>>newArrayList()),
                Collections.synchronizedList(Lists.<RemoveAgentMessage<A>>newArrayList()),
                Collections.synchronizedList(Lists.<DeliverAgentMessageMessage<A>>newArrayList()));
    }

    private void activateAgentInternal(A agent, Initializer<? super A> initializer) {
        assert agent != null : "population is null";
        assert initializer != null : "initializer is null";

        initializer.initialize(agent);
        space.insertObject(agent, agent.getProjection());
        agent.activate(ActiveSimulationContext.<S, A>create(self(), agentIdSequence.incrementAndGet(), getStep() + 1));

        LOGGER.debug("Agent activated: {}", agent);

        simulationLogger.logAgentCreation(agent);
    }

    protected abstract S self();

    private void passivateAgentsInternal(List<? extends A> agents) {
        for (A agent : agents) {
            agent.shutDown(PassiveSimulationContext.<S,A>instance());
            releaseAgent(agent);
        }
        space.removeInactiveAgents();
    }

    @Override
    public void removeAgent(final A agent) {
        checkNotNull(agent);
        checkArgument(agent.simulation().equals(this));
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

    private A createAgentInternal(final Population population) {
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
    public int getStep() {
        return currentStep.get();
    }

    @Override
    public synchronized void nextStep() {

        final int step = currentStep.incrementAndGet();

        LOGGER.info("{}: Entering step {}; {}", this, step, countAgents());

        executeAllAgents();

        processAgentMessageDelivery();
        processRequestedAgentRemovals();
        processAgentsMovement();
        processRequestedAgentActivations();

        afterStepCleanUp();
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
        final RecursiveAction executeAllAgents = RecursiveActions.foreach(getAgents(), new VoidFunction<Simulatable>() {
            @Override
            public void process(Simulatable agent) {
                agent.execute();
            }
        }, parallelizationThreshold);
        forkJoinPool.invoke(executeAllAgents);
    }

    private void processRequestedAgentActivations() {
        for (AddAgentMessage<A> addAgentMessage : addAgentMessages) {
            activateAgentInternal(createAgentInternal(addAgentMessage.population), addAgentMessage.initializer);
        }
        addAgentMessages.clear();
    }

    private void processAgentsMovement() {
        final RecursiveAction moveAllAgents = RecursiveActions.foreach(getAgents(), new VoidFunction<A>() {
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
    public Object snapshotValue(String key, Supplier<Object> valueCalculator) {
        if (!snapshotValues.containsKey(key))
            snapshotValues.putIfAbsent(key, valueCalculator.get());
        return snapshotValues.get(key);
    }

    @Override
    public void createAgent(Population population, Initializer<? super A> initializer) {
        addAgentMessages.add(new AddAgentMessage<A>(population, initializer));
    }

    @Override
    public SimulationLogger getSimulationLogger() {
        return simulationLogger;
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

    private static class AddAgentMessage<T> {

        private final Population population;
        private final Initializer<? super T> initializer;

        private AddAgentMessage(Population population, Initializer<? super T> initializer) {
            assert population != null;
            assert initializer != null;
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

    private static class AgentSpace<Z extends Space2D<T, P>, T extends Agent<?, ?, P>, P extends Object2D> extends ForwardingSpace2D<T, P> {

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
            assert population != null;
            return agentsByPopulation.get(population).size();
        }

        @Override
        public boolean insertObject(T object, P projection) {
            assert object != null;
            if (super.insertObject(object, projection)) {
                final boolean add = agentsByPopulation.get(object.getPopulation()).add(object);
                assert add : "Could not add " + object;
                return true;
            }
            return false;
        }

        @Override
        public boolean removeObject(T agent) {
            assert agent != null;
            if (super.removeObject(agent)) {
                final boolean remove = agentsByPopulation.get(agent.getPopulation()).remove(agent);
                assert remove : "Could not remove " + agent;
                return true;
            }
            return false;
        }

        @Override
        public P getProjection(T object) {
            return object.getProjection();
        }

        public void removeInactiveAgents() {
            if (super.removeIf(INACTIVE_AGENT_PREDICATE)) {
                Iterables.removeIf(agentsByPopulation.values(), INACTIVE_AGENT_PREDICATE);
            }
        }

        public Iterable<T> getAgents(Population population) {
            assert population != null;
            return agentsByPopulation.get(population);
        }
    }

    public static <A extends Agent<A, S, P>, S extends SpatialSimulation<A, Z>, Z extends Space2D<A, P>, P extends Object2D> ParallelizedSimulationBuilder<A,S,Z,P> builder(Z space, Set<A> prototypes) {
        return new ParallelizedSimulationBuilder<A, S, Z, P>(space, prototypes);
    }

    public static class ParallelizedSimulationBuilder<A extends Agent<A, S, P>, S extends SpatialSimulation<A, Z>, Z extends Space2D<A, P>, P extends Object2D> implements Builder<ParallelizedSimulation<A, S, Z,P>> {

        private KeyedObjectPool<Population, A> agentPool;
        private int parallelizationThreshold = 1000;
        private final Z space;
        private final Set<A> prototypes;
        private SimulationLogger simulationLogger = new ConsoleLogger();

        public ParallelizedSimulationBuilder(Z space, Set<A> prototypes) {
            this.space = checkNotNull(space);
            this.prototypes = checkNotNull(prototypes);
        }

        @Override
        public ParallelizedSimulation<A, S, Z, P> build() throws IllegalStateException {
            checkState(agentPool != null, "No AgentPool has been defined");
            checkState(!prototypes.contains(null), "Prototypes contains null");
            checkState(space.isEmpty(), "Space is not empty");

            return new ParallelizedSimulation<A, S, Z, P>(this);
        }

        public ParallelizedSimulationBuilder agentPool(KeyedObjectPool<Population, A> pool) {
            this.agentPool = checkNotNull(pool);
            return this;
        }

        public ParallelizedSimulationBuilder parallelizationThreshold(int parallelizationThreshold) {
            checkArgument(parallelizationThreshold > 0, "parallelizationThreshold must be positive");
            this.parallelizationThreshold = parallelizationThreshold;
            return this;
        }

        public ParallelizedSimulationBuilder simulationLogger(SimulationLogger simulationLogger) {
            this.simulationLogger = simulationLogger;
            return this;
        }
    }
}
