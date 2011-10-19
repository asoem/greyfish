package org.asoem.greyfish.core.simulation;

import com.google.common.base.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.*;
import javolution.util.FastList;
import jsr166y.ForkJoinPool;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.concurrent.SingletonForkJoinPool;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.AgentMessage;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.space.Coordinates2D;
import org.asoem.greyfish.core.space.ImmutableCoordinates2D;
import org.asoem.greyfish.core.space.MovingObject2D;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.lang.Command;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.lang.ImmutableMapBuilder;
import org.asoem.greyfish.utils.ConcurrentIterables;
import org.asoem.greyfish.utils.Counter;
import org.asoem.greyfish.utils.ListenerSupport;
import org.asoem.greyfish.utils.PolarPoint;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.*;
import static org.asoem.greyfish.core.simulation.SimulationMessageType.*;
import static org.asoem.greyfish.core.space.ImmutableCoordinates2D.sum;

/**
 * A {@code Simulation} that uses a {@link ForkJoinPool} to execute {@link Agent}s
 * and process their addition, removal, migration and communication in parallel.
 */
public class ParallelizedSimulation implements Simulation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelizedSimulation.class);

    private final Multimap<SimulationMessageType, Command> commandListMap =
            Multimaps.synchronizedMultimap(
                    Multimaps.newListMultimap(
                            Maps.<SimulationMessageType, Collection<Command>>newEnumMap(SimulationMessageType.class),
                            new Supplier<List<Command>>() {
                                @Override
                                public List<Command> get() {
                                    return new ArrayList<Command>();
                                }
                            }
                    )
            );

    private final BiMap<Population, Agent> populationPrototypeMap;
    private final Map<Population, Counter> populationCounterMap;

    private final FastList<Agent> agents = FastList.newInstance();

    private final Cache<Integer, Agent> agentCache = CacheBuilder.newBuilder()
            .weakValues()
            .build(new CacheLoader<Integer, Agent>() {
                @Override
                public Agent load(final Integer integer) throws Exception {
                    return Iterables.find(getAgents(), new Predicate<Agent>() {
                        @Override
                        public boolean apply(Agent receiver) {
                            return receiver.getId().equals(integer);
                        }
                    });
                }
            });

    private final ListenerSupport<SimulationListener> listenerSupport = ListenerSupport.newInstance();

    private final KeyedObjectPool objectPool = new StackKeyedObjectPool(
            new BaseKeyedPoolableObjectFactory() {

                @Override
                public Object makeObject(Object key) throws Exception {
                    assert key != null;
                    assert key instanceof Population;

                    Population population = (Population) key;
                    return ImmutableAgent.cloneOf(populationPrototypeMap.get(population));
                }
            },
            10000, 100);

    private AtomicInteger maxId = new AtomicInteger();

    private final Scenario scenario;

    private final TiledSpace space;

    private int steps = 0;

    private String title = "untitled";

    public ParallelizedSimulation(final Scenario scenario) {
        checkNotNull(scenario);

        this.scenario = scenario;

        this.populationPrototypeMap = ImmutableBiMap.copyOf(ImmutableMapBuilder.<Population, Agent>newInstance().
                putAll(scenario.getPrototypes(),
                        new Function<Agent, Population>() {
                            @Override
                            public Population apply(@Nullable Agent agent) {
                                assert agent != null;
                                return agent.getPopulation();
                            }
                        },
                        Functions.<Agent>identity()).
                build());

        this.populationCounterMap = ImmutableMapBuilder.<Population,Counter>newInstance().
                putAll(populationPrototypeMap.keySet(),
                        Functions.<Population>identity(),
                        new Function<Population, Counter>() {
                            @Override
                            public Counter apply(@Nullable Population population) {
                                return new Counter(0);
                            }
                        }).
                build();

        this.space = new TiledSpace(scenario.getSpace());

        initialize();

        if (LOGGER.isTraceEnabled())
            listenerSupport.addListener(new SimulationListener() {
                @Override
                public void eventFired(SimulationEvent event) {
                    LOGGER.trace("End of simulation step " + event.getSource().getSteps());
                }
            });
    }

    public static ParallelizedSimulation newSimulation(final Scenario scenario) {
        return new ParallelizedSimulation(scenario);
    }

    @Override
    public int numberOfPopulations() {
        return this.populationPrototypeMap.size();
    }

    @Override
    public Iterable<MovingObject2D> findObjects(Coordinates2D coordinates, double radius) {
        return space.findObjects(coordinates, radius);
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
        for (Agent placeholder : scenario.getPlaceholder()) {
            final Agent clone = newAgentFromPool(placeholder.getPopulation());
            addAgent(clone, placeholder.getCoordinates());
        }
    }

    @Override
    public Collection<Agent> getAgents() {
        return agents;
    }

    private void addAgent(Agent agent, Coordinates2D coordinates) {
        checkNotNull(agent);
        checkArgument(populationPrototypeMap.containsKey(agent.getPopulation()),
                "The population " + agent.getPopulation() + " of the given agent is unknown for this simulation");
        // populationCounterMap is guaranteed to contain exactly the same keys as populationPrototypeMap

        checkNotNull(coordinates);
        checkArgument(space.covers(coordinates),
                "Coordinates " + coordinates + " do not fall inside the area of this simulation's space: " + space);

        agent.setAnchorPoint(ImmutableCoordinates2D.at(coordinates));
        agent.prepare(this);

        LOGGER.trace("{}: Adding Agent {}", this, agent);

        // following actions must be synchronized
        synchronized (this) {
            agents.add(agent);
            Counter counter = populationCounterMap.get(agent.getPopulation());
            counter.increase(); // non-null verified by checkCanAddAgent();
        }
    }

    private void removeAgentInternal(Agent agent) {
        checkNotNull(agent);

        // save as populationCounterMap does not allow null elements
        populationCounterMap.get(agent.getPopulation()).decrease();

        agent.shutDown();
        putCloneInPool(agent);
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
        commandListMap.put(REMOVE_AGENT,
                new Command() {
                    @Override
                    public void execute() {
                        removeAgentInternal(agent);
                    }
                });
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
    public void addSimulationListener(SimulationListener listener) {
        listenerSupport.addListener(listener);
    }

    @Override
    public void removeSimulationListener(SimulationListener listener) {
        listenerSupport.removeListener(listener);
    }

    @Override
    public int generateAgentID() {
        return maxId.incrementAndGet();
    }

    @Override
    public void createAgent(final Population population, final Coordinates2D coordinates, final Genome genome) {
        checkNotNull(population);
        checkNotNull(coordinates);
        checkNotNull(genome);

        checkState(populationPrototypeMap.containsKey(population));

        final Agent agent = newAgentFromPool(population);
        agent.injectGamete(genome);

        commandListMap.put(ADD_AGENT,
                new Command() {
                    @Override
                    public void execute() {
                        addAgent(agent, coordinates);
                    }
                });
    }

    /**
     * Request an {@code Agent} from the {@code objectPool}
     * @param population the {@code Population} of the requested {@code Agent}
     * @return a new or recycled {@code Agent}
     * @throws RuntimeException if no non-null {@code Agent} could be retrieved from the {@code objectPool}
     */
    private Agent newAgentFromPool(final Population population) {
        Agent ret = null;

        try {
            ret = (Agent) objectPool.borrowObject(population);
        } catch (Exception e) {
            LOGGER.error("Error getting Agent from objectPool for population {}", population.getName(), e);
        }

        assert ret != null : "Agent is null for population " + population.getName();
        return ret;
    }

    @Override
    public Set<Agent> getPrototypes() {
        return populationPrototypeMap.values();
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

        try {

            Collection<Command> deliverMessageCommands = commandListMap.get(DELIVER_AGENT_MESSAGE);
            final CountDownLatch deliverMessageCommandsLatch = new CountDownLatch(deliverMessageCommands.size());
            SingletonForkJoinPool.execute(
                    ConcurrentIterables.create(deliverMessageCommands, new Functor<Command>() {
                        @Override
                        public void apply(@Nullable Command command) {
                            command.execute();
                            deliverMessageCommandsLatch.countDown();
                        }
                    }, 1000));
            deliverMessageCommandsLatch.await();

            Collection<Command> removeAgentCommands = commandListMap.get(REMOVE_AGENT);
            final CountDownLatch removeAgentCommandsLatch = new CountDownLatch(deliverMessageCommands.size());
            SingletonForkJoinPool.execute(
                    ConcurrentIterables.create(removeAgentCommands, new Functor<Command>() {
                        @Override
                        public void apply(@Nullable Command command) {
                            command.execute();
                            removeAgentCommandsLatch.countDown();
                        }
                    }, 1000));
            removeAgentCommandsLatch.await();

            for (Agent agent : getAgents()) {
                final PolarPoint motion = agent.getMotionVector();
                if (motion.getDistance() != 0)
                    getSpace().moveObject(agent, sum(agent.getCoordinates(), motion.toCartesian()));
            }

            Collection<Command> addAgentCommands = commandListMap.get(ADD_AGENT);
            final CountDownLatch addAgentCommandsLatch = new CountDownLatch(deliverMessageCommands.size());
            SingletonForkJoinPool.execute(
                    ConcurrentIterables.create(addAgentCommands, new Functor<Command>() {
                        @Override
                        public void apply(@Nullable Command command) {
                            command.execute();
                            addAgentCommandsLatch.countDown();
                        }
                    }, 1000));
            addAgentCommandsLatch.await();

            space.updateTopo(agents);

            final CountDownLatch processAgentsLatch = new CountDownLatch(agents.size());
            SingletonForkJoinPool.execute(
                    ConcurrentIterables.create(agents, new Functor<Agent>() {
                        @Override
                        public void apply(Agent agent) {
                            agent.execute();
                            processAgentsLatch.countDown();
                        }
                    }, 1000));
            processAgentsLatch.await();

        } catch (InterruptedException ie) {
            LOGGER.error("Simulation was not able to wait for all processes to successfully execute", ie);
        }

        commandListMap.clear();

        ++steps;
        notifyStep();
    }

    private void notifyStep() {
        listenerSupport.notifyListeners(new Functor<SimulationListener>() {

            @Override
            public void apply(SimulationListener listener) {
                listener.eventFired(new SimulationEvent(ParallelizedSimulation.this, SimulationEvent.Event.STEP));
            }
        });
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
        commandListMap.put(DELIVER_AGENT_MESSAGE,
                new Command() {
                    @Override
                    public void execute() {
                        for (Agent agent : message.getRecipients()) {
                            agent.receive(new AgentMessage(message, getSteps()));
                        }
                    }
                });
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
}
