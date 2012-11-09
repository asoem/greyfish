package org.asoem.greyfish.core.simulation;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.*;
import jsr166y.ForkJoinPool;
import jsr166y.RecursiveAction;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.*;
import org.asoem.greyfish.core.io.ConsoleLogger;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.space.ForwardingSpace2D;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.base.Builder;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Initializer;
import org.asoem.greyfish.utils.base.VoidFunction;
import org.asoem.greyfish.utils.concurrent.RecursiveActions;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

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
public class ParallelizedSimulation<A extends Agent, S extends Space2D<A>> extends AbstractSimulation<A, S> {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(ParallelizedSimulation.class);

    @Element(name = "space")
    private final AgentSpace<A, S> space;
    @Attribute
    private final AtomicInteger currentStep = new AtomicInteger(-1);
    @ElementList(name = "addAgentMessages", required = false, empty = false, entry = "addAgentMessage", inline = true)
    private final List<AddAgentMessage> addAgentMessages;
    @ElementList(name = "removeAgentMessages", required = false, empty = false, entry = "removeAgentMessage", inline = true)
    private final List<RemoveAgentMessage> removeAgentMessages;
    @ElementList(name = "deliverAgentMessageMessages", required = false, empty = false, entry = "deliverAgentMessageMessage", inline = true)
    private final List<DeliverAgentMessageMessage> deliverAgentMessageMessages;
    private final KeyedObjectPool<Population, A> agentPool;
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();
    private final ConcurrentMap<String, Object> snapshotValues;
    @Attribute(name = "parallelizationThreshold")
    private final int parallelizationThreshold;
    @ElementList(name = "prototypes")
    private final Set<A> prototypes;
    @Element(name = "simulationLogger")
    private final SimulationLogger simulationLogger;
    private final AtomicInteger agentIdSequence = new AtomicInteger();
    @Attribute
    private String title = "untitled";

    private ParallelizedSimulation(ParallelizedSimulationBuilder<A, S> builder) {
        this(builder.space,
                builder.prototypes,
                builder.parallelizationThreshold,
                builder.simulationLogger,
                builder.agentPool,
                Collections.synchronizedList(Lists.<AddAgentMessage>newArrayList()),
                Collections.synchronizedList(Lists.<RemoveAgentMessage>newArrayList()),
                Collections.synchronizedList(Lists.<DeliverAgentMessageMessage>newArrayList()));
    }

    private ParallelizedSimulation(
                                  @Element(name = "space") S space,
                                  @ElementList(name = "prototypes") Set<A> prototypes,
                                  @Attribute(name = "parallelizationThreshold") int parallelizationThreshold,
                                  @Element(name = "simulationLogger") SimulationLogger simulationLogger,
                                  @Element(name = "agentPool") KeyedObjectPool<Population, A> agentPool,
                                  @ElementList(name = "addAgentMessages", required = false, empty = false, entry = "addAgentMessage", inline = true) List<AddAgentMessage> addAgentMessages,
                                  @ElementList(name = "removeAgentMessages", required = false, empty = false, entry = "removeAgentMessage", inline = true) List<RemoveAgentMessage> removeAgentMessages,
                                  @ElementList(name = "deliverAgentMessageMessages", required = false, empty = false, entry = "deliverAgentMessageMessage", inline = true) List<DeliverAgentMessageMessage> deliverAgentMessageMessages) {

        this.prototypes = prototypes;
        this.parallelizationThreshold = parallelizationThreshold;
        this.agentPool = agentPool;
        this.space = new AgentSpace(space);
        this.simulationLogger = simulationLogger;

        this.addAgentMessages = addAgentMessages;
        this.removeAgentMessages = removeAgentMessages;
        this.deliverAgentMessageMessages = deliverAgentMessageMessages;

        this.snapshotValues = Maps.newConcurrentMap();
    }

    private static KeyedObjectPool<Population, Agent> createDefaultAgentPool(final Set<Agent> prototypes) {
        checkArgument(!prototypes.contains(null));

        return new StackKeyedObjectPool<Population, Agent>(
                new BaseKeyedPoolableObjectFactory<Population, Agent>() {

                    final Map<Population, Agent> populationPrototypeMap =
                            Maps.uniqueIndex(prototypes, new Function<Agent, Population>() {
                                @Override
                                public Population apply(Agent input) {
                                    return input.getPopulation();
                                }
                            });

                    @Override
                    public Agent makeObject(Population key) throws Exception {
                        assert key != null;

                        final Agent prototype = populationPrototypeMap.get(key);
                        assert prototype != null : "Found no Prototype for " + key;

                        final Agent clone = DeepCloner.clone(prototype, Agent.class);

                        return FrozenAgent.builder(prototype.getPopulation())
                                .addActions(clone.getActions())
                                .addProperties(clone.getProperties())
                                .addTraits(clone.getTraits())
                                .build();
                    }
                },
                10000, 100);
    }

    private void activateAgentInternal(A agent, Initializer<? super A> initializer) {
        assert agent != null : "population is null";
        assert initializer != null : "initializer is null";

        initializer.initialize(agent);
        space.insertObject(agent);
        agent.activate(ActiveSimulationContext.create(this, agentIdSequence.incrementAndGet(), getStep() + 1));

        LOGGER.debug("Agent activated: {}", agent);

        simulationLogger.logAgentCreation(agent);
    }

    private void passivateAgentsInternal(List<? extends Agent> agents) {
        for (Agent agent : agents) {
            agent.shutDown(PassiveSimulationContext.instance());
            releaseAgent(agent);
        }
        space.removeInactiveAgents();
    }

    @Override
    public void removeAgent(final Agent agent) {
        checkNotNull(agent);
        checkArgument(agent.simulation().equals(this));
        removeAgentMessages.add(new RemoveAgentMessage(agent));
    }

    private void releaseAgent(final Agent agent) {
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
    public S getSpace() {
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
                space.moveObject(agent);
            }
        }, parallelizationThreshold);
        forkJoinPool.invoke(moveAllAgents);
    }

    private void processRequestedAgentRemovals() {
        LOGGER.debug("Removing {} agent(s)", removeAgentMessages.size());
        if (removeAgentMessages.size() > 0) {
            passivateAgentsInternal(Lists.transform(removeAgentMessages, new Function<RemoveAgentMessage, Agent>() {
                @Override
                public Agent apply(RemoveAgentMessage removeAgentMessage) {
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

    private static class AddAgentMessage<A extends Agent> {

        private final Population population;
        private final Initializer<? super A> initializer;

        private AddAgentMessage(Population population, Initializer<? super A> initializer) {
            assert population != null;
            assert initializer != null;
            this.population = population;
            this.initializer = initializer;
        }
    }

    private static class RemoveAgentMessage {

        private final Agent agent;

        public RemoveAgentMessage(Agent agent) {
            assert agent != null;
            this.agent = agent;
        }

    }

    private static class DeliverAgentMessageMessage<A extends Agent> {

        private final ACLMessage<A> message;

        public DeliverAgentMessageMessage(ACLMessage<A> message) {
            this.message = message;
        }

    }

    private static class AgentSpace<A extends Agent, S extends Space2D<A>> extends ForwardingSpace2D<A> {

        private final S delegate;
        private final Multimap<Population, A> agentsByPopulation;
        private final Predicate<A> INACTIVE_AGENT_PREDICATE = new Predicate<A>() {
            @Override
            public boolean apply(A input) {
                return !input.isActive();
            }
        };

        private AgentSpace(S delegate) {
            assert delegate != null;

            this.delegate = delegate;
            this.agentsByPopulation = LinkedListMultimap.create();
        }

        @Override
        protected S delegate() {
            return delegate;
        }

        public int count(Population population) {
            assert population != null;
            return agentsByPopulation.get(population).size();
        }

        @Override
        public boolean insertObject(A agent, double x, double y, double orientation) {
            assert agent != null;
            if (super.insertObject(agent, x, y, orientation)) {
                final boolean add = agentsByPopulation.get(agent.getPopulation()).add(agent);
                assert add : "Could not add " + agent;
                return true;
            }
            return false;
        }

        @Override
        public boolean insertObject(A agent) {
            assert agent != null;
            if (super.insertObject(agent)) {
                final boolean add = agentsByPopulation.get(agent.getPopulation()).add(agent);
                assert add : "Could not add " + agent;
                return true;
            }
            return false;
        }

        @Override
        public boolean removeObject(A agent) {
            assert agent != null;
            if (super.removeObject(agent)) {
                final boolean remove = agentsByPopulation.get(agent.getPopulation()).remove(agent);
                assert remove : "Could not remove " + agent;
                return true;
            }
            return false;
        }

        public void removeInactiveAgents() {
            if (super.removeIf(INACTIVE_AGENT_PREDICATE)) {
                Iterables.removeIf(agentsByPopulation.values(), INACTIVE_AGENT_PREDICATE);
            }
        }

        public Iterable<A> getAgents(Population population) {
            assert population != null;
            return agentsByPopulation.get(population);
        }
    }

    public static <A extends Agent, S extends Space2D<A>> ParallelizedSimulationBuilder<A,S> builder(S space, Set<A> prototypes) {
        return new ParallelizedSimulationBuilder<A,S>(space, prototypes);
    }

    public static class ParallelizedSimulationBuilder<A extends Agent, S extends Space2D<A>> implements Builder<ParallelizedSimulation<A, S>> {

        private KeyedObjectPool<Population, A> agentPool;
        private int parallelizationThreshold = 1000;
        private S space;
        private Set<A> prototypes;
        private SimulationLogger simulationLogger = new ConsoleLogger();

        public ParallelizedSimulationBuilder(S space, Set<A> prototypes) {
            this.space = checkNotNull(space);
            this.prototypes = checkNotNull(prototypes);
        }

        @Override
        public ParallelizedSimulation<A,S> build() throws IllegalStateException {
            if (agentPool == null)
                agentPool = createDefaultAgentPool(prototypes);

            checkState(!prototypes.contains(null), "Prototypes contains null");
            checkState(space.isEmpty(), "Space is not empty");

            return new ParallelizedSimulation<A,S>(this);
        }

        public ParallelizedSimulationBuilder<A,S> agentPool(KeyedObjectPool<Population, A> pool) {
            this.agentPool = checkNotNull(pool);
            return this;
        }

        public ParallelizedSimulationBuilder<A,S> parallelizationThreshold(int parallelizationThreshold) {
            checkArgument(parallelizationThreshold > 0, "parallelizationThreshold must be positive");
            this.parallelizationThreshold = parallelizationThreshold;
            return this;
        }

        public ParallelizedSimulationBuilder<A,S> simulationLogger(SimulationLogger simulationLogger) {
            this.simulationLogger = simulationLogger;
            return this;
        }
    }
}
