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
import org.asoem.greyfish.utils.space.SpatialObject;
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
public class ParallelizedSimulation<P extends Object2D> extends AbstractSimulation<P> {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(ParallelizedSimulation.class);

    @Element(name = "space")
    private final AgentSpace<P> space;
    @Attribute
    private final AtomicInteger currentStep = new AtomicInteger(-1);
    @ElementList(name = "addAgentMessages", required = false, empty = false, entry = "addAgentMessage", inline = true)
    private final List<AddAgentMessage> addAgentMessages;
    @ElementList(name = "removeAgentMessages", required = false, empty = false, entry = "removeAgentMessage", inline = true)
    private final List<RemoveAgentMessage<Agent>> removeAgentMessages;
    @ElementList(name = "deliverAgentMessageMessages", required = false, empty = false, entry = "deliverAgentMessageMessage", inline = true)
    private final List<DeliverAgentMessageMessage> deliverAgentMessageMessages;
    private final KeyedObjectPool<Population, Agent> agentPool;
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();
    private final ConcurrentMap<String, Object> snapshotValues;
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
        this(builder.space,
                builder.prototypes,
                builder.parallelizationThreshold,
                builder.simulationLogger,
                builder.agentPool,
                Collections.synchronizedList(Lists.<AddAgentMessage>newArrayList()),
                Collections.synchronizedList(Lists.<RemoveAgentMessage<Agent>>newArrayList()),
                Collections.synchronizedList(Lists.<DeliverAgentMessageMessage>newArrayList()));
    }

    private ParallelizedSimulation(
                                  @Element(name = "space") Space2D<? extends Agent, ?> space,
                                  @ElementList(name = "prototypes") Set<Agent> prototypes,
                                  @Attribute(name = "parallelizationThreshold") int parallelizationThreshold,
                                  @Element(name = "simulationLogger") SimulationLogger simulationLogger,
                                  @Element(name = "agentPool") KeyedObjectPool<Population, Agent> agentPool,
                                  @ElementList(name = "addAgentMessages", required = false, empty = false, entry = "addAgentMessage", inline = true) List<AddAgentMessage> addAgentMessages,
                                  @ElementList(name = "removeAgentMessages", required = false, empty = false, entry = "removeAgentMessage", inline = true) List<RemoveAgentMessage<Agent>> removeAgentMessages,
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

    private void activateAgentInternal(Agent agent, Initializer<? super Agent> initializer) {
        assert agent != null : "population is null";
        assert initializer != null : "initializer is null";

        initializer.initialize(agent);
        space.insertObject(agent, agent.getProjection());
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
        removeAgentMessages.add(new RemoveAgentMessage<Agent>(agent));
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
    public Space2D<Agent,SpatialObject> getSpace() {
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
        for (DeliverAgentMessageMessage message : deliverAgentMessageMessages) {
            for (Agent agent : message.message.getRecipients()) {
                agent.receive(new AgentMessage<Agent>(message.message, getStep()));
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
        for (AddAgentMessage addAgentMessage : addAgentMessages) {
            activateAgentInternal(createAgentInternal(addAgentMessage.population), addAgentMessage.initializer);
        }
        addAgentMessages.clear();
    }

    private void processAgentsMovement() {
        final RecursiveAction moveAllAgents = RecursiveActions.foreach(getAgents(), new VoidFunction<Agent>() {
            @Override
            public void process(Agent agent) {
                space.moveObject(agent, agent.getMotion());
            }
        }, parallelizationThreshold);
        forkJoinPool.invoke(moveAllAgents);
    }

    private void processRequestedAgentRemovals() {
        LOGGER.debug("Removing {} agent(s)", removeAgentMessages.size());
        if (removeAgentMessages.size() > 0) {
            passivateAgentsInternal(Lists.transform(removeAgentMessages, new Function<RemoveAgentMessage<Agent>, Agent>() {
                @Override
                public Agent apply(RemoveAgentMessage<Agent> removeAgentMessage) {
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

    @Override
    public Iterable<Agent> getAgents(Population population) {
        return space.getAgents(population);
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

    private static class RemoveAgentMessage<A> {

        private final A agent;

        public RemoveAgentMessage(A agent) {
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

    private static class AgentSpace<P extends Object2D> extends ForwardingSpace2D<Agent, P> implements Space2D<Agent, P> {

        private final Space2D<Agent, P> delegate;
        private final Multimap<Population, Agent> agentsByPopulation;
        private final Predicate<Agent> INACTIVE_AGENT_PREDICATE = new Predicate<Agent>() {
            @Override
            public boolean apply(Agent input) {
                return !input.isActive();
            }
        };

        private AgentSpace(Space2D<Agent, P> delegate) {
            assert delegate != null;

            this.delegate = delegate;
            this.agentsByPopulation = LinkedListMultimap.create();
        }

        @Override
        protected Space2D<Agent, P> delegate() {
            return delegate;
        }

        public int count(Population population) {
            assert population != null;
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
        public boolean insertObject(Agent object, P projection) {
            assert object != null;
            if (super.insertObject(object, projection)) {
                final boolean add = agentsByPopulation.get(object.getPopulation()).add(object);
                assert add : "Could not add " + object;
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

        @Override
        public P getProjection(Agent object) {
            return object.getProjection();
        }

        public void removeInactiveAgents() {
            if (super.removeIf(INACTIVE_AGENT_PREDICATE)) {
                Iterables.removeIf(agentsByPopulation.values(), INACTIVE_AGENT_PREDICATE);
            }
        }

        public Iterable<Agent> getAgents(Population population) {
            assert population != null;
            return agentsByPopulation.get(population);
        }
    }

    public static ParallelizedSimulationBuilder builder(Space2D<Agent, Object2D> space, Set<? extends Agent> prototypes) {
        return new ParallelizedSimulationBuilder(space, prototypes);
    }

    public static class ParallelizedSimulationBuilder implements Builder<ParallelizedSimulation> {

        private KeyedObjectPool<Population, Agent> agentPool;
        private int parallelizationThreshold = 1000;
        private final Space2D<? extends Agent, ?> space;
        private final Set<Agent> prototypes;
        private SimulationLogger simulationLogger = new ConsoleLogger();

        public ParallelizedSimulationBuilder(Space2D<? extends Agent,?> space, Set<? extends Agent> prototypes) {
            this.space = checkNotNull(space);
            this.prototypes = ImmutableSet.copyOf(checkNotNull(prototypes));
        }

        @Override
        public ParallelizedSimulation build() throws IllegalStateException {
            checkState(agentPool != null, "No AgentPool has been defined");
            checkState(!prototypes.contains(null), "Prototypes contains null");
            checkState(space.isEmpty(), "Space is not empty");

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
