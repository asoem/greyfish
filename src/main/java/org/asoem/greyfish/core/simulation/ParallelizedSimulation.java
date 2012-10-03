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
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.AgentMessage;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.space.ForwardingTiledSpace;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.core.space.WalledTile;
import org.asoem.greyfish.core.space.WalledTileSpace;
import org.asoem.greyfish.utils.base.Builder;
import org.asoem.greyfish.utils.base.Initializer;
import org.asoem.greyfish.utils.base.Initializers;
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@code Simulation} that uses a {@link ForkJoinPool} to execute {@link Agent}s
 * and process their addition, removal, migration and communication in parallel.
 */
public class ParallelizedSimulation extends AbstractSimulation {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(ParallelizedSimulation.class);

    @Element(name = "space")
    private final AgentSpace space;
    @Attribute
    private final AtomicInteger currentStep = new AtomicInteger(-1);
    @ElementList(name = "addAgentMessages", required = false, empty = false, entry = "addAgentMessage", inline = true)
    private final List<AddAgentMessage> addAgentMessages = Collections.synchronizedList(Lists.<AddAgentMessage>newArrayList());
    @ElementList(name = "removeAgentMessages", required = false, empty = false, entry = "removeAgentMessage", inline = true)
    private final List<RemoveAgentMessage> removeAgentMessages = Collections.synchronizedList(Lists.<RemoveAgentMessage>newArrayList());
    @ElementList(name = "deliverAgentMessageMessages", required = false, empty = false, entry = "deliverAgentMessageMessage", inline = true)
    private final List<DeliverAgentMessageMessage> deliverAgentMessageMessages = Collections.synchronizedList(Lists.<DeliverAgentMessageMessage>newArrayList());
    private final KeyedObjectPool<Population, Agent> agentPool;
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();
    private final ConcurrentMap<String, Object> snapshotValues = Maps.newConcurrentMap();
    @Attribute(name = "parallelizationThreshold")
    private final int parallelizationThreshold;
    @ElementList(name = "prototypes")
    private final Set<Agent> prototypes;
    @Element(name = "simulationLogger")
    private final SimulationLogger simulationLogger;
    private final AtomicInteger agentIdSequence = new AtomicInteger();
    @Attribute
    private String title = "untitled";

    private ParallelizedSimulation(ParallelizedSimulationBuilder builder) {
        this.prototypes = builder.prototypes;
        this.space = new AgentSpace(WalledTileSpace.createEmptyCopy(builder.space));
        this.agentPool = builder.agentPool;
        this.parallelizationThreshold = builder.parallelizationThreshold;
        this.simulationLogger = builder.simulationLogger;
    }

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private ParallelizedSimulation(@Attribute(name = "parallelizationThreshold") int parallelizationThreshold,
                                  @Element(name = "space") WalledTileSpace<Agent> space,
                                  @ElementList(name = "prototypes") Set<Agent> prototypes,
                                  @Element(name = "simulationLogger") SimulationLogger simulationLogger,
                                  @ElementList(name = "addAgentMessages", required = false, empty = false, entry = "addAgentMessage", inline = true) List<AddAgentMessage> addAgentMessages,
                                  @ElementList(name = "removeAgentMessages", required = false, empty = false, entry = "removeAgentMessage", inline = true) List<RemoveAgentMessage> removeAgentMessages,
                                  @ElementList(name = "deliverAgentMessageMessages", required = false, empty = false, entry = "deliverAgentMessageMessage", inline = true) List<DeliverAgentMessageMessage> deliverAgentMessageMessages) {

        this.prototypes = prototypes;
        this.parallelizationThreshold = parallelizationThreshold;
        this.agentPool = createDefaultAgentPool(prototypes);
        this.space = new AgentSpace(WalledTileSpace.createEmptyCopy(space));
        this.simulationLogger = simulationLogger;
        this.addAgentMessages.addAll(addAgentMessages);
        this.removeAgentMessages.addAll(removeAgentMessages);
        this.deliverAgentMessageMessages.addAll(deliverAgentMessageMessages);

        for (final Agent templateAgent : space.getObjects()) {
            activateAgentInternal(templateAgent, Initializers.emptyInitializer());
        }
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

                        return ImmutableAgent.fromPrototype(prototype);
                    }
                },
                10000, 100);
    }

    private void activateAgentInternal(Agent agent, Initializer<? super Agent> initializer) {
        assert agent != null : "population is null";
        assert initializer != null : "initializer is null";

        initializer.initialize(agent);
        space.insertObject(agent);
        agent.activate(this);

        LOGGER.debug("Agent got activated: {}", agent);

        simulationLogger.logAgentCreation(agent);
    }

    private void passivateAgentsInternal(List<? extends Agent> agents) {
        for (Agent agent : agents) {
            agent.shutDown();
            releaseAgent(agent);
        }

        switch (agents.size()) {
            case 1: space.removeObject(agents.get(0));
            default: space.removeInactiveAgents();
        }
    }

    /**
     * Check if the given {@code agent} can be added to this simulation
     *
     * @param agent an {@code Agent}
     */
    private void checkCanAddAgent(final Agent agent) {
        checkArgument(ImmutableAgent.class.isInstance(agent), // also checks for null
                "Agent must be of type " + ImmutableAgent.class);

    }

    @Override
    public void removeAgent(final Agent agent) {
        checkNotNull(agent);
        checkArgument(agent.simulation().equals(this));
        removeAgentMessages.add(new RemoveAgentMessage(agent));
    }

    private void releaseAgent(final Agent agent) {
        checkCanAddAgent(agent);
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

    @Override
    public int generateAgentID() {
        return agentIdSequence.incrementAndGet();
    }

    private Agent createAgentInternal(final Population population) {
        checkNotNull(population);
        try {
            final Agent agent = agentPool.borrowObject(population);
            checkNotNull(agent, "borrowObject in agentPool returned null");
            agent.initialize();
            return agent;
        } catch (Exception e) {
            LOGGER.error("Couldn't borrow Agent from agentPool for population {}", population.getName(), e);
            throw new AssertionError(e);
        }
    }

    @Override
    public Set<Agent> getPrototypes() {
        return prototypes;
    }

    @Override
    public TiledSpace<Agent, WalledTile> getSpace() {
        return space;
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
        for (DeliverAgentMessageMessage message : deliverAgentMessageMessages) {
            for (Agent agent : message.message.getRecipients()) {
                agent.receive(new AgentMessage(message.message, getStep()));
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
        tryInvoke(executeAllAgents);
    }

    private void processRequestedAgentActivations() {
        for (AddAgentMessage addAgentMessage : addAgentMessages) {
            activateAgentInternal(createAgentInternal(addAgentMessage.population), addAgentMessage.initializer);
        }
        addAgentMessages.clear();
    }

    private void processAgentsMovement() {
        final RecursiveAction moveAllAgents = RecursiveActions.foreach(getAgents(), new VoidFunction<Agent>() {
            @Override
            public void process(Agent agent) {
                space.moveObject(agent);
            }
        }, parallelizationThreshold);
        tryInvoke(moveAllAgents);
    }

    private void tryInvoke(RecursiveAction recursiveAction) {
        forkJoinPool.invoke(recursiveAction);
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
    public void deliverMessage(final ACLMessage<Agent> message) {
        checkNotNull(message);
        deliverAgentMessageMessages.add(new DeliverAgentMessageMessage(message));
    }

    @Override
    public Object snapshotValue(String key, Supplier<Object> valueCalculator) {
        if (!snapshotValues.containsKey(key))
            snapshotValues.putIfAbsent(key, valueCalculator.get());
        return snapshotValues.get(key);
    }

    @Override
    public void createAgent(Population population, Initializer<? super Agent> initializer) {
        addAgentMessages.add(new AddAgentMessage(population, initializer));
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

    private static class AddAgentMessage {

        private final Population population;
        private final Initializer<? super Agent> initializer;

        private AddAgentMessage(Population population, Initializer<? super Agent> initializer) {
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

    private static class DeliverAgentMessageMessage {

        private final ACLMessage<Agent> message;

        public DeliverAgentMessageMessage(ACLMessage<Agent> message) {
            this.message = message;
        }

    }

    private static class AgentSpace extends ForwardingTiledSpace<Agent, WalledTile> {

        private final TiledSpace<Agent, WalledTile> delegate;
        private final Multimap<Population, Agent> agentsByPopulation;

        private AgentSpace(TiledSpace<Agent, WalledTile> delegate) {
            assert delegate != null;

            this.delegate = delegate;
            this.agentsByPopulation = LinkedListMultimap.create();
        }

        @Override
        protected TiledSpace<Agent, WalledTile> delegate() {
            return delegate;
        }

        public int count(Population population) {
            return agentsByPopulation.get(population).size();
        }

        @Override
        public boolean insertObject(Agent agent, double x, double y, double orientation) {
            assert agent != null;
            if (super.insertObject(agent, x, y, orientation)) {
                final boolean add = agentsByPopulation.get(agent.getPopulation()).add(agent);
                assert add : "Could not add " + agent;
                return true;
            }
            return false;
        }

        @Override
        public boolean removeObject(Agent agent) {
            assert agent != null;
            if (super.removeObject(agent)) {
                final boolean remove = agentsByPopulation.get(agent.getPopulation()).remove(agent);
                assert remove : "Could not remove " + agent;
                return true;
            }
            return false;
        }

        public void removeInactiveAgents() {
            final Predicate<Agent> agentPredicate = new Predicate<Agent>() {
                @Override
                public boolean apply(Agent input) {
                    return !input.isActive();
                }
            };
            if (super.removeIf(agentPredicate)) {
                Iterables.removeIf(agentsByPopulation.values(), agentPredicate);
            }
        }
    }

    public static ParallelizedSimulationBuilder builder(WalledTileSpace<Agent> space, Set<Agent> prototypes) {
        return new ParallelizedSimulationBuilder(space, prototypes);
    }

    public static class ParallelizedSimulationBuilder implements Builder<ParallelizedSimulation> {

        private KeyedObjectPool<Population, Agent> agentPool;
        private int parallelizationThreshold = 1000;
        private WalledTileSpace<Agent> space;
        private Set<Agent> prototypes;
        private SimulationLogger simulationLogger;

        public ParallelizedSimulationBuilder(WalledTileSpace<Agent> space, Set<Agent> prototypes) {
            this.space = checkNotNull(space);
            checkArgument(space.isEmpty(), "Space is not empty");
            this.prototypes = checkNotNull(prototypes);
            checkArgument(!prototypes.contains(null), "prototypes contains null");
        }

        @Override
        public ParallelizedSimulation build() throws IllegalStateException {
            if (agentPool == null)
                agentPool = createDefaultAgentPool(prototypes);

            return new ParallelizedSimulation(this);
        }

        public ParallelizedSimulationBuilder agentPool(KeyedObjectPool<Population, Agent> pool) {
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
