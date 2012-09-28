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
import org.asoem.greyfish.core.io.NullLogger;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.space.ForwardingTiledSpace;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.core.space.WalledTile;
import org.asoem.greyfish.core.space.WalledTileSpace;
import org.asoem.greyfish.utils.base.VoidFunction;
import org.asoem.greyfish.utils.concurrent.RecursiveActions;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;
import org.asoem.greyfish.utils.space.Object2D;
import org.asoem.greyfish.utils.space.Point2D;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@code Simulation} that uses a {@link ForkJoinPool} to execute {@link Agent}s
 * and process their addition, removal, migration and communication in parallel.
 */
public class ParallelizedSimulation implements Simulation {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(ParallelizedSimulation.class);

    @Element(name = "space")
    private final AgentSpace space;

    @Attribute
    private AtomicInteger currentStep = new AtomicInteger(-1);

    @Attribute
    private String title = "untitled";

    @ElementList(name = "addAgentMessages", required = false, empty = false, entry = "addAgentMessage", inline = true)
    private final List<AddAgentMessage> addAgentMessages =
            Collections.synchronizedList(Lists.<AddAgentMessage>newArrayList());

    @ElementList(name = "removeAgentMessages", required = false, empty = false, entry = "removeAgentMessage", inline = true)
    private final List<RemoveAgentMessage> removeAgentMessages =
            Collections.synchronizedList(Lists.<RemoveAgentMessage>newArrayList());

    @ElementList(name = "deliverAgentMessageMessages", required = false, empty = false, entry = "deliverAgentMessageMessage", inline = true)
    private final List<DeliverAgentMessageMessage> deliverAgentMessageMessages =
            Collections.synchronizedList(Lists.<DeliverAgentMessageMessage>newArrayList());

    private final KeyedObjectPool<Population, Agent> objectPool = new StackKeyedObjectPool<Population, Agent>(
            new BaseKeyedPoolableObjectFactory<Population, Agent>() {

                @Override
                public Agent makeObject(Population key) throws Exception {
                    assert key != null;

                    final Agent prototype = getPrototype(key);
                    assert prototype != null : "Found no Prototype for " + key;

                    return ImmutableAgent.fromPrototype(prototype);
                }

                @Override
                public void activateObject(Population key, Agent obj) throws Exception {
                    obj.initialize();
                }
            },
            10000, 100);

    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    private final UUID uuid = UUID.randomUUID();

    private SimulationLogger simulationLogger = new NullLogger();

    private final ConcurrentMap<String, Object> snapshotValues = Maps.newConcurrentMap();

    @Attribute(name = "parallelizationThreshold")
    private final int parallelizationThreshold;

    @Nullable
    private Agent getPrototype(final Population population) {
        return space.prototypeFor(population);
    }

    private final AtomicInteger agentIdSequence = new AtomicInteger();

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    public ParallelizedSimulation(@Attribute(name = "parallelizationThreshold") int parallelizationThreshold,
                                  @Element(name = "space") WalledTileSpace<Agent> space,
                                  @ElementList(name = "addAgentMessages", required = false, empty = false, entry = "addAgentMessage", inline = true) List<AddAgentMessage> addAgentMessages,
                                  @ElementList(name = "removeAgentMessages", required = false, empty = false, entry = "removeAgentMessage", inline = true) List<RemoveAgentMessage> removeAgentMessages,
                                  @ElementList(name = "deliverAgentMessageMessages", required = false, empty = false, entry = "deliverAgentMessageMessage", inline = true) List<DeliverAgentMessageMessage> deliverAgentMessageMessages) {
        this(parallelizationThreshold, space);
        this.addAgentMessages.addAll(addAgentMessages);
        this.removeAgentMessages.addAll(removeAgentMessages);
        this.deliverAgentMessageMessages.addAll(deliverAgentMessageMessages);
    }

    public ParallelizedSimulation(int parallelizationThreshold, WalledTileSpace<Agent> space) {
        checkNotNull(space);
        this.parallelizationThreshold = parallelizationThreshold;

        Set<Agent> prototypes = ImmutableSet.copyOf(Iterables.transform(space.getObjects(), new Function<Agent, Agent>() {
            final Map<Population, Agent> populationAgentMap = Maps.newHashMap();

            @Override
            public Agent apply(@Nullable Agent agent) {
                assert agent != null;
                final Population population = agent.getPopulation();
                if (!populationAgentMap.containsKey(population))
                    populationAgentMap.put(population, agent);
                return populationAgentMap.get(population);
            }
        }));
        this.space = new AgentSpace(WalledTileSpace.createEmptyCopy(space), prototypes);

        for (Agent agent : space.getObjects()) {
            activateAgentInternal(agent, agent.getProjection());
        }
    }

    @Override
    public int numberOfPopulations() {
        return getPrototypes().size();
    }

    @Override
    public Iterable<Agent> findNeighbours(Agent agent, double radius) {
        return Iterables.filter(space.getVisibleNeighbours(agent, radius), Agent.class);
    }

    @Override
    public Iterable<Agent> getAgents(final Population population) {
        checkNotNull(population);

        return Iterables.filter(getAgents(), new Predicate<Agent>() {
            @Override
            public boolean apply(Agent agent) {
                return agent.hasPopulation(population);
            }
        });
    }

    @Override
    public List<Agent> getAgents() {
        return space.getObjects();
    }

    private void activateAgentInternal(Agent agent, Object2D projection) {
        assert agent != null : "agent is null";
        assert projection != null : "projection is null";
        final Point2D anchorPoint = projection.getAnchorPoint();
        space.insertObject(agent, anchorPoint.getX(), anchorPoint.getY(), projection.getOrientationAngle());
        agent.activate(this);
        LOGGER.debug("Agent got activated: {}", agent);
        simulationLogger.addAgent(agent);
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
            objectPool.returnObject(agent.getPopulation(), agent);
        } catch (Exception e) {
            LOGGER.error("Error in prototype pool", e);
        }
    }

    @Override
    public int countAgents() {
        return space.countObjects();
    }

    @Override
    public int countAgents(Population population) {
        return space.count(population);
    }

    @Override
    public int generateAgentID() {
        return agentIdSequence.incrementAndGet();
    }

    @Override
    public void activateAgent(Agent agent, Object2D projection) {
        checkNotNull(agent);
        checkNotNull(projection);

        checkArgument(getPrototype(agent.getPopulation()) != null,
                "The population " + agent.getPopulation() + " of the given agent is unknown for this simulation");
        final Point2D anchorPoint = projection.getAnchorPoint();
        checkArgument(space.contains(anchorPoint.getX(), anchorPoint.getY()),
                "Coordinates of " + projection + " do not fall inside the area of this simulation's space: " + space);

        addAgentMessages.add(new AddAgentMessage(agent, projection));
    }

    @Override
    public Agent createAgent(final Population population) {
        checkNotNull(population);
        try {
            return objectPool.borrowObject(population);
        } catch (Exception e) {
            LOGGER.error("Couldn't borrow Agent from objectPool for population {}", population.getName(), e);
            throw new AssertionError(e);
        }
    }

    @Override
    public Set<Agent> getPrototypes() {
        return space.prototypes();
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
            activateAgentInternal(addAgentMessage.agent, addAgentMessage.location);
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
    public String toString() {
        return "Simulation['" + getName() + "', " + getUUID() + "]";
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
    public void deliverMessage(final ACLMessage<Agent> message) {
        checkNotNull(message);
        deliverAgentMessageMessages.add(new DeliverAgentMessageMessage(message));
    }

    @Override
    public void shutdown() {
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public void setSimulationLogger(SimulationLogger simulationLogger) {
        this.simulationLogger = checkNotNull(simulationLogger);
    }

    @Override
    public void createEvent(int agentId, String populationName, double[] coordinates, Object eventOrigin, String title, String message) {
        simulationLogger.addEvent(
                uuid, currentStep.get(),
                agentId, populationName, coordinates,
                eventOrigin.getClass().getSimpleName(), title, message);
    }

    @Override
    public Object snapshotValue(String key, Supplier<Object> valueCalculator) {
        if (!snapshotValues.containsKey(key))
            snapshotValues.putIfAbsent(key, valueCalculator.get());
        return snapshotValues.get(key);
    }

    private static class AddAgentMessage {

        private final Agent agent;
        private final Object2D location;

        private AddAgentMessage(Agent agent, Object2D location) {
            this.agent = agent;
            this.location = location;
        }
    }

    private static class RemoveAgentMessage {

        private final Agent agent;

        public RemoveAgentMessage(Agent agent) {
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
        private final Map<Population, Agent> populationPrototypeMap;
        private final Set<Agent> prototypes;

        private AgentSpace(TiledSpace<Agent, WalledTile> delegate, Set<Agent> prototypes) {
            assert delegate != null;
            assert prototypes != null;

            this.delegate = delegate;
            this.prototypes = prototypes;
            this.agentsByPopulation = LinkedListMultimap.create(prototypes.size());
            this.populationPrototypeMap = Maps.uniqueIndex(prototypes, new Function<Agent, Population>() {
                @Override
                public Population apply(Agent prototype) {
                    return prototype.getPopulation();
                }
            });
        }

        @Override
        protected TiledSpace<Agent, WalledTile> delegate() {
            return delegate;
        }

        public Agent prototypeFor(Population population) {
            return populationPrototypeMap.get(population);
        }

        public int count(Population population) {
            return agentsByPopulation.get(population).size();
        }

        public Set<Agent> prototypes() {
            return prototypes;
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
}
