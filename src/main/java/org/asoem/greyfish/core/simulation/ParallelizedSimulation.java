package org.asoem.greyfish.core.simulation;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jsr166y.ForkJoinPool;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.AgentMessage;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.io.SimulationLoggerProvider;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.base.VoidFunction;
import org.asoem.greyfish.utils.collect.ImmutableMapBuilder;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.asoem.greyfish.utils.space.ImmutableMotion2D;
import org.asoem.greyfish.utils.space.ImmutableObject2D;
import org.asoem.greyfish.utils.space.Location2D;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.core.concurrent.SingletonForkJoinPool.invoke;
import static org.asoem.greyfish.utils.parallel.ParallelIterables.apply;

/**
 * A {@code Simulation} that uses a {@link ForkJoinPool} to execute {@link Agent}s
 * and process their addition, removal, migration and communication in parallel.
 */
public class ParallelizedSimulation implements Simulation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelizedSimulation.class);

    @Element(name = "space")
    private final TiledSpace<Agent> space;

    @Attribute
    private int steps = -1;

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

    private final Map<Population, AtomicInteger> populationCounterMap;

    private final KeyedObjectPool<Population, Agent> objectPool = new StackKeyedObjectPool<Population, Agent>(
            new BaseKeyedPoolableObjectFactory<Population, Agent>() {

                @Override
                public Agent makeObject(Population key) throws Exception {
                    assert key != null;

                    final Agent prototype = getPrototype(key);
                    assert prototype != null : "Found no Prototype for " + key;

                    return ImmutableAgent.cloneOf(prototype);
                }
            },
            10000, 100);

    private final Set<Agent> prototypes;

    @Nullable
    private Agent getPrototype(final Population population) {
        return Iterables.find(prototypes, new Predicate<Agent>() {
            @Override
            public boolean apply(@Nullable Agent agent) {
                assert agent != null;
                return agent.getPopulation().equals(population);
            }
        }, null);
    }

    private final AtomicInteger maxId = new AtomicInteger();

    /**
     * 
     * @param scenario the scenario
     */
    public ParallelizedSimulation(final Scenario scenario) {
        this(new TiledSpace<Agent>(scenario.getSpace(), new Function<Agent, Agent>() {
            @Override
            public Agent apply(@Nullable Agent agent) {
                return ImmutableAgent.cloneOf(agent);
            }
        }));
    }

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    public ParallelizedSimulation(@Element(name = "space") TiledSpace<Agent> space,
                                  @ElementList(name = "addAgentMessages", required = false, empty = false, entry = "addAgentMessage", inline = true) List<AddAgentMessage> addAgentMessages,
                                  @ElementList(name = "removeAgentMessages", required = false, empty = false, entry = "removeAgentMessage", inline = true) List<RemoveAgentMessage> removeAgentMessages,
                                  @ElementList(name = "deliverAgentMessageMessages", required = false, empty = false, entry = "deliverAgentMessageMessage", inline = true) List<DeliverAgentMessageMessage> deliverAgentMessageMessages) {
        this(space);
        this.addAgentMessages.addAll(addAgentMessages);
        this.removeAgentMessages.addAll(removeAgentMessages);
        this.deliverAgentMessageMessages.addAll(deliverAgentMessageMessages);
    }

    public ParallelizedSimulation(@Element(name = "space") TiledSpace<Agent> space) {
        checkNotNull(space);
        this.space = space;

        this.prototypes = ImmutableSet.copyOf(Iterables.transform(space.getObjects(), new Function<Agent, Agent>() {
            final Map<Population, Agent> populationAgentMap = Maps.newHashMap();
            @Override
            public Agent apply(@Nullable Agent agent) {
                assert agent != null;
                final Population population = agent.getPopulation();
                if (! populationAgentMap.containsKey(population))
                    populationAgentMap.put(population, agent);
                return populationAgentMap.get(population);
            }
        }));

        this.populationCounterMap = ImmutableMapBuilder.<Population, AtomicInteger>newInstance().
                putAll(prototypes,
                        new Function<Agent, Population>() {
                            @Override
                            public Population apply(Agent agent) {
                                return agent.getPopulation();
                            }
                        },
                        Functions.forSupplier(new Supplier<AtomicInteger>() {
                            @Override
                            public AtomicInteger get() {
                                return new AtomicInteger(0);
                            }
                        })).
                build();
        
        for (Agent agent : space.getObjects()) {
            agentAdded(agent);
        }
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
        return Iterables.filter(space.getNeighbours(agent, radius), Agent.class);
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

    @Override
    public Iterable<Agent> getAgents() {
        return space.getObjects();
    }

    private void addAgentInternal(Agent agent, Location2D locatable) {
        checkNotNull(agent);
        checkArgument(getPrototype(agent.getPopulation()) != null,
                "The population " + agent.getPopulation() + " of the given agent is unknown for this simulation");
        // populationCounterMap is guaranteed to contain exactly the same keys as populationPrototypeMap

        checkNotNull(locatable, "You have to provide a location for the Agent");
        checkArgument(space.contains(locatable),
                "Coordinates " + locatable + " do not fall inside the area of this simulation's space: " + space);

        // following actions must be synchronized
        space.addObject(agent, ImmutableObject2D.of(locatable.getX(), locatable.getY(), 0));
        agentAdded(agent);
    }

    private void agentAdded(Agent agent) {
        agent.prepare(this);
        AtomicInteger counter = populationCounterMap.get(agent.getPopulation());
        counter.incrementAndGet(); // non-null verified by checkCanAddAgent();
        LOGGER.trace("{}: Agent added: {}", this, agent);
    }

    private void removeAgentsInternal(Collection<? extends Agent> agents) {
        for (Agent agent : agents) {
            space.removeObject(agent);
            populationCounterMap.get(agent.getPopulation()).decrementAndGet();
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
        checkArgument(agent.getSimulationContext().getSimulation().equals(this));
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
        return space.countObjects();
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
    public void createAgent(final Population population, final Chromosome<? extends Gene<?>> chromosome, Location2D location) {
        checkNotNull(population);
        checkArgument(getPrototype(population) != null);
        checkNotNull(chromosome);
        checkNotNull(location);

        addAgentMessages.add(new AddAgentMessage(population, chromosome, location));
    }

    /**
     * Request an {@code Agent} from the {@code objectPool}
     * @param population the {@code Population} of the requested {@code Agent}
     * @return a new or recycled {@code Agent}
     * @throws RuntimeException if no non-null {@code Agent} could be retrieved from the {@code objectPool}
     */
    private Agent borrowAgentFromPool(final Population population) {
        try {
            return objectPool.borrowObject(population);
        } catch (Exception e) {
            LOGGER.error("Error getting Agent from objectPool for population {}", population.getName(), e);
            throw new AssertionError(e);
        }
    }

    @Override
    public Set<Agent> getPrototypes() {
        return prototypes;
    }

    @Override
    public TiledSpace<Agent> getSpace() {
        return space;
    }

    @Override
    public int getSteps() {
        return steps;
    }

    @Override
    public synchronized void step() {
        ++steps;

        LOGGER.debug("{}: Entering step {}", this, steps);

        executeAllAgents();

        processAgentMessageDelivery();
        processRequestedAgentRemovals();
        processAgentsMovement();
        processRequestedAgentAdditions();

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
        invoke(apply(getAgents(), new VoidFunction<Simulatable>() {
            @Override
            public void apply(Simulatable agent) {
                agent.execute();
            }
        }, 1000));
    }

    private void processRequestedAgentAdditions() {
        for (AddAgentMessage addAgentMessage : addAgentMessages) {
            final Agent clone = borrowAgentFromPool(addAgentMessage.population);
            clone.injectGamete(addAgentMessage.chromosome);
            addAgentInternal(clone, ImmutableObject2D.of(addAgentMessage.location.getX(), addAgentMessage.location.getY(), 0));
        }
        addAgentMessages.clear();
    }

    private void processAgentsMovement() {
        invoke(apply(getAgents(), new VoidFunction<Agent>() {
            @Override
            public void apply(Agent agent) {
                if (agent.getMotion() != ImmutableMotion2D.noMotion())
                    space.moveObject(agent, agent.getMotion());
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
    public String toString() {
        return "Simulation['" + getName() + "']@" + getSteps();
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
        SimulationLoggerProvider.getLogger(this).close();
    }

    /**
     * Creates a new {@code Simulation} and calls {@link #step()} until the {@code stopTrigger} returns {@code true}
     * @param scenario the {@code Scenario} used to initialize this Simulation
     * @param stopTrigger the {@code Predicate} which will be asked before each simulation step if the simulation should stop.
     * @return the newly created simulation
     */
    @SuppressWarnings("UnusedDeclaration")
    public static ParallelizedSimulation runScenario(Scenario scenario, Predicate<? super ParallelizedSimulation> stopTrigger) {
        checkNotNull(scenario);
        checkNotNull(stopTrigger);

        ParallelizedSimulation simulation = new ParallelizedSimulation(scenario);

        while (!stopTrigger.apply(simulation)) {
            simulation.step();
        }

        return simulation;
    }

    private static class AddAgentMessage {

        private final Population population;
        private final Chromosome<? extends Gene<?>> chromosome;
        private final Location2D location;

        public AddAgentMessage(Population population, Chromosome<? extends Gene<?>> chromosome, Location2D location) {
            this.population = population;
            this.chromosome = chromosome;
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
}
