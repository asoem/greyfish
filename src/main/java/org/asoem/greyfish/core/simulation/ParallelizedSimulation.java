package org.asoem.greyfish.core.simulation;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import javolution.util.FastList;
import jsr166y.ForkJoinPool;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.individual.*;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.base.Counter;
import org.asoem.greyfish.utils.base.VoidFunction;
import org.asoem.greyfish.utils.collect.ImmutableMapBuilder;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.asoem.greyfish.utils.space.Coordinates2D;
import org.asoem.greyfish.utils.space.Movable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static org.asoem.greyfish.core.concurrent.SingletonForkJoinPool.invoke;
import static org.asoem.greyfish.core.simulation.SimulationMessageType.*;
import static org.asoem.greyfish.utils.parallel.ParallelIterables.apply;

/**
 * A {@code Simulation} that uses a {@link ForkJoinPool} to execute {@link Agent}s
 * and process their addition, removal, migration and communication in parallel.
 */
public class ParallelizedSimulation implements Simulation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelizedSimulation.class);

    private final List<AddAgentMessage> addAgentMessages =
            Collections.synchronizedList(Lists.<AddAgentMessage>newArrayList());

    private final List<RemoveAgentMessage> removeAgentMessages =
            Collections.synchronizedList(Lists.<RemoveAgentMessage>newArrayList());

    private final List<DeliverAgentMessageMessage> deliverAgentMessageMessages =
            Collections.synchronizedList(Lists.<DeliverAgentMessageMessage>newArrayList());

    private final Map<Population, Counter> populationCounterMap;

    private final FastList<Agent> agents = FastList.newInstance();

    private final KeyedObjectPool objectPool = new StackKeyedObjectPool(
            new BaseKeyedPoolableObjectFactory() {

                @Override
                public Object makeObject(Object key) throws Exception {
                    assert key != null;
                    assert key instanceof Population;

                    Agent prototype = getPrototype((Population) key);
                    assert prototype != null : "Found no Prototype for " + key;

                    return ImmutableAgent.cloneOf(prototype);
                }
            },
            10000, 100);

    @Nullable
    private Agent getPrototype(Population population) {
        return scenario.getPrototype(population);
    }

    private AtomicInteger maxId = new AtomicInteger();

    private final Scenario scenario;

    private final TiledSpace space;

    private int steps = 0;

    private String title = "untitled";

    public ParallelizedSimulation(final Scenario scenario) {
        checkNotNull(scenario);

        this.scenario = scenario;

        this.populationCounterMap = ImmutableMapBuilder.<Population,Counter>newInstance().
                putAll(getPrototypes(),
                        new Function<Agent, Population>() {
                            @Override
                            public Population apply(Agent agent) {
                                return agent.getPopulation();
                            }
                        },
                        new Function<Agent, Counter>() {
                            @Override
                            public Counter apply(Agent population) {
                                return new Counter(0);
                            }
                        }).
                build();

        this.space = new TiledSpace(scenario.getSpace());

        initialize();
    }

    public static ParallelizedSimulation newSimulation(final Scenario scenario) {
        return new ParallelizedSimulation(scenario);
    }

    @Override
    public int numberOfPopulations() {
        return getPrototypes().size();
    }

    @Override
    public Iterable<Agent> findNeighbours(Agent agent, double radius) {
        return Iterables.filter(Iterables.filter(space.findObjects(agent, radius), not(equalTo((Movable) agent))), Agent.class);
    }

    @Override
    public Iterable<Agent> getAgents(final Population population) {
        checkNotNull(population);

        return Iterables.filter(getAgents(), new Predicate<Agent>() {
            @Override
            public boolean apply(Agent agent) {
                return agent.getPopulation().equals(population);
            }
        });
    }

    private void initialize() {
        // convert each placeholder to a concrete object
        for (Placeholder placeholder : scenario.getPlaceholder()) {
            final Agent clone = newAgentFromPool(placeholder.getPopulation());
            assert clone != null;
            addAgentInternal(clone, placeholder.getCoordinates());
        }
    }

    @Override
    public Collection<Agent> getAgents() {
        return agents;
    }

    private void addAgentInternal(Agent agent, Coordinates2D coordinates) {
        checkNotNull(agent);
        checkArgument(getPrototype(agent.getPopulation()) != null,
                "The population " + agent.getPopulation() + " of the given agent is unknown for this simulation");
        // populationCounterMap is guaranteed to contain exactly the same keys as populationPrototypeMap

        checkNotNull(coordinates);
        checkArgument(space.covers(coordinates),
                "Coordinates " + coordinates + " do not fall inside the area of this simulation's space: " + space);

        agent.prepare(this);

        LOGGER.trace("{}: Adding Agent {}", this, agent);

        // following actions must be synchronized
        synchronized (this) {
            agents.add(agent);
            space.addObject(agent, coordinates);
            Counter counter = populationCounterMap.get(agent.getPopulation());
            counter.increase(); // non-null verified by checkCanAddAgent();
        }
    }

    private void removeAgentsInternal(Collection<? extends Agent> agents) {
        this.agents.removeAll(agents);

        for (Agent agent : agents) {
            space.removeObject(agent);
            populationCounterMap.get(agent.getPopulation()).decrease();
            agent.shutDown();
            putCloneInPool(agent);
        }
    }

    /**
     * Check if the given {@code agent} can be added to this simulation
     * @param agent an {@code Agent}
     */
    private void checkCanAddAgent(final Agent agent) {
        checkArgument(ImmutableAgent.class.isInstance(agent), // also checks for null
                "Agent must be of type " + ImmutableAgent.class);

    }

    @Override
    public void removeAgent(final Agent agent) {
        checkNotNull(agent);
        checkArgument(agent.getSimulation().equals(this));
        removeAgentMessages.add(new RemoveAgentMessage(agent));
    }

    private void putCloneInPool(final Agent agent) {
        checkCanAddAgent(agent);
        try {
            objectPool.returnObject(agent.getPopulation(), agent);
        } catch (Exception e) {
            LOGGER.error("Error in prototype pool", e);
        }
    }

    @Override
    public int countAgents() {
        return agents.size();
    }

    @Override
    public int countAgents(Population population) {
        checkArgument(populationCounterMap.containsKey(population), "Population unknown: " + population);
        return populationCounterMap.get(population).get();
    }

    @Override
    public int generateAgentID() {
        return maxId.incrementAndGet();
    }

    @Override
    public void createAgent(final Population population, final Genome genome, Coordinates2D location) {
        checkNotNull(population);
        checkArgument(getPrototype(population) != null);
        checkNotNull(genome);
        checkNotNull(location);

        addAgentMessages.add(new AddAgentMessage(population, genome, location));
    }

    /**
     * Request an {@code Agent} from the {@code objectPool}
     * @param population the {@code Population} of the requested {@code Agent}
     * @return a new or recycled {@code Agent}
     * @throws RuntimeException if no non-null {@code Agent} could be retrieved from the {@code objectPool}
     */
    private Agent newAgentFromPool(final Population population) {
        try {
            return Agent.class.cast(objectPool.borrowObject(population));
        } catch (Exception e) {
            LOGGER.error("Error getting Agent from objectPool for population {}", population.getName(), e);
            throw new AssertionError(e);
        }
    }

    @Override
    public Set<Agent> getPrototypes() {
        return scenario.getPrototypes();
    }

    @Override
    public TiledSpace getSpace() {
        return space;
    }

    @Override
    public int getSteps() {
        return steps;
    }

    @Override
    public synchronized void step() {

        LOGGER.trace("{}: Entering step {}", this, steps);

        executeAllAgents();

        processAgentMessageDelivery();
        processRequestedAgentRemovals();
        processAgentsMovement();
        processRequestedAgentAdditions();

        ++steps;
    }

    private void processAgentMessageDelivery() {
        for (DeliverAgentMessageMessage message : deliverAgentMessageMessages) {
            for (Agent agent : message.message.getRecipients()) {
                agent.receive(new AgentMessage(message.message, getSteps()));
            }
        }
        deliverAgentMessageMessages.clear();
    }

    private void executeAllAgents() {
        invoke(apply(agents, new VoidFunction<Agent>() {
            @Override
            public void apply(Agent agent) {
                agent.execute();
            }
        }, 1000));
    }

    private void processRequestedAgentAdditions() {
        for (AddAgentMessage addAgentMessage : addAgentMessages) {
            Agent clone = newAgentFromPool(addAgentMessage.population);
            clone.injectGamete(addAgentMessage.genome);
            addAgentInternal(clone, addAgentMessage.location);
        }
        addAgentMessages.clear();
    }

    private void processAgentsMovement() {
        invoke(apply(agents, new VoidFunction<Agent>() {
            @Override
            public void apply(Agent agent) {
                space.moveObject(agent);
            }
        }, 1000));
    }

    private void processRequestedAgentRemovals() {
        removeAgentsInternal(Lists.transform(removeAgentMessages, new Function<RemoveAgentMessage, Agent>() {
            @Override
            public Agent apply(RemoveAgentMessage removeAgentMessage) {
                return removeAgentMessage.agent;
            }
        }));
        removeAgentMessages.clear();
    }

    @Override
    public Scenario getScenario() {
        return scenario;
    }

    @Override
    public String toString() {
        return "Simulation[" + getName() + "]#" + getSteps();
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public boolean hasName(@Nullable String s) {
        return Objects.equal(title, s);
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

    /**
     * Creates a new {@code Simulation} and calls {@link #step()} until the {@code stopTrigger} returns {@code true}
     * @param scenario the {@code Scenario} used to initialize this Simulation
     * @param stopTrigger the {@code Predicate} which will be asked before each simulation step if the simulation should stop.
     * @return the newly created simulation
     */
    public static ParallelizedSimulation runScenario(Scenario scenario, Predicate<? super ParallelizedSimulation> stopTrigger) {
        checkNotNull(scenario);
        checkNotNull(stopTrigger);

        ParallelizedSimulation simulation = new ParallelizedSimulation(scenario);

        while (!stopTrigger.apply(simulation)) {
            simulation.step();
        }

        return simulation;
    }

    private static interface SimulationMessage {
        SimulationMessageType messageType();
    }

    private static class AddAgentMessage implements SimulationMessage {

        private final Population population;
        private final Genome genome;
        private final Coordinates2D location;

        public AddAgentMessage(Population population, Genome genome, Coordinates2D location) {
            this.population = population;
            this.genome = genome;
            this.location = location;
        }

        @Override
        public SimulationMessageType messageType() {
            return ADD_AGENT;
        }
    }

    private static class RemoveAgentMessage implements SimulationMessage {

        private final Agent agent;

        public RemoveAgentMessage(Agent agent) {
            this.agent = agent;
        }

        @Override
        public SimulationMessageType messageType() {
            return REMOVE_AGENT;
        }
    }

    private static class DeliverAgentMessageMessage implements SimulationMessage {

        private final ACLMessage<Agent> message;

        public DeliverAgentMessageMessage(ACLMessage<Agent> message) {
            this.message = message;
        }

        @Override
        public SimulationMessageType messageType() {
            return DELIVER_AGENT_MESSAGE;
        }
    }
}
