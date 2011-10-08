package org.asoem.greyfish.core.simulation;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import javolution.util.FastList;
import jsr166y.ForkJoinPool;
import jsr166y.RecursiveAction;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.PostOffice;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.individual.Agent;
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
import org.asoem.greyfish.utils.Counter;
import org.asoem.greyfish.utils.ListenerSupport;
import org.asoem.greyfish.utils.PolarPoint;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.*;
import static org.asoem.greyfish.core.space.ImmutableCoordinates2D.sum;

/**
 * A {@code Simulation} that uses a {@link ForkJoinPool} to execute {@link Agent}s
 * and process their addition, removal, migration and communication in parallel.
 */
public class ParallelizedSimulation implements Simulation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelizedSimulation.class);

    private final BiMap<Population, Agent> prototypeMap;
    private final Map<Population, Counter> populationCounterMap;

    private final PostOffice postOffice = PostOffice.newInstance();

    private final ForkJoinPool forkJoinPool = new ForkJoinPool();
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    private enum CommandType {
        MESSAGE,
        AGENT_REMOVE,
        AGENT_ADD
    }

    private final Multimap<CommandType, Command> commandListMap =
            Multimaps.synchronizedMultimap(HashMultimap.<CommandType, Command>create());

    private final FastList<Agent> agents = FastList.newInstance();
    private final Collection<Agent> threadSaveAgents = agents.shared();

    private final ListenerSupport<SimulationListener> listenerSupport = ListenerSupport.newInstance();

    private final KeyedObjectPool objectPool = new StackKeyedObjectPool(
            new BaseKeyedPoolableObjectFactory() {

                @Override
                public Object makeObject(Object key) throws Exception {
                    assert key != null;
                    assert key instanceof Population;

                    Population population = (Population) key;
                    return ImmutableAgent.cloneOf(prototypeMap.get(population));
                }
            },
            10000, 100);

    private AtomicInteger maxId = new AtomicInteger();

    private final Scenario scenario;

    private final TiledSpace space;

    private int steps = 0;

    private String title = "untitled";

    private ParallelizedSimulation(final Scenario scenario) {
        checkNotNull(scenario);

        this.scenario = scenario;

        this.prototypeMap = ImmutableBiMap.copyOf(ImmutableMapBuilder.<Population, Agent>newInstance().
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
                putAll(prototypeMap.keySet(),
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
        return this.prototypeMap.size();
    }

    @Override
    public Iterable<MovingObject2D> findObjects(Coordinates2D coordinates, double radius) {
        return space.findObjects(coordinates, radius);
    }

    @Override
    public Iterable<Agent> getAgents(final Population population) {
        checkNotNull(population);

        return Iterables.filter(threadSaveAgents, new Predicate<Agent>() {
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
        return Collections.unmodifiableCollection(threadSaveAgents);
    }

    private void addAgent(Agent agent, Coordinates2D coordinates) {
        checkNotNull(agent);
        checkArgument(prototypeMap.containsKey(agent.getPopulation()),
                "The population " + agent.getPopulation() + " of the given agent is unknown for this simulation");
        // populationCounterMap is guaranteed to contain exactly the same keys as prototypeMap

        checkNotNull(coordinates);
        checkArgument(space.covers(coordinates),
                "Coordinates " + coordinates + " do not fall inside the area of this simulation's space: " + space);

        agent.setAnchorPoint(ImmutableCoordinates2D.at(coordinates));
        agent.prepare(this);

        LOGGER.trace("{}: Adding Agent {}", this, agent);

        // following actions must be synchronized
        threadSaveAgents.add(agent);
        Counter counter = populationCounterMap.get(agent.getPopulation());
        counter.increase(); // non-null verified by checkCanAddAgent();
        space.addOccupant(agent);
    }

    private void removeAgentInternal(Agent agent) {
        checkNotNull(agent);

        if (!space.removeOccupant(agent))
            throw new RuntimeException("Agent " + agent + " couldn't be removed from " + space);

        if (!threadSaveAgents.remove(agent))
            throw new RuntimeException("Agent " + agent + " couldn't be removed from " + threadSaveAgents);

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

        /*
           * TODO: removal could be implemented more efficiently.
           * e.g. by marking agents and removal during a single iteration over all
           */
        commandListMap.put(CommandType.AGENT_REMOVE,
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
        return threadSaveAgents.size();
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

        checkState(prototypeMap.containsKey(population));

        final Agent agent = newAgentFromPool(population);
        agent.injectGamete(genome);

        commandListMap.put(CommandType.AGENT_ADD,
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
        return prototypeMap.values();
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
        updateEnvironment();
        processAgents();

        ++steps;
        notifyStep();
    }

    private void processAgents() {
        LOGGER.debug("{}: processing {} Agents", this, agents.size());
        if (agents.size() > 0)
            forkJoinPool.invoke(new ProcessAgentsForked(agents.head(), agents.size(), Math.max(agents.size(), 1000) / 2));
    }

    private class ProcessAgentsForked extends RecursiveAction {

        private final FastList.Node<Agent> node;
        private final int nNodes;
        private final int forkThreshold;

        private ProcessAgentsForked(final FastList.Node<Agent> node, final int nNodes, final int forkThreshold) {
            assert node != null;
            assert nNodes > 0;

            this.node = node;
            this.nNodes = nNodes;
            this.forkThreshold = forkThreshold;
        }

        @Override
        protected void compute() {
            if (nNodes > forkThreshold) {

                // split list
                final int splitAtIndex = nNodes / 2;
                FastList.Node<Agent> iterNode = node;
                for (int i = splitAtIndex; i-- >= 0;) {
                    iterNode = iterNode.getNext();
                }

                final FastList.Node<Agent> splitNode = iterNode;

                // fork
                final ProcessAgentsForked left = new ProcessAgentsForked(node, splitAtIndex, forkThreshold);
                final ProcessAgentsForked right = new ProcessAgentsForked(splitNode.getPrevious(), nNodes - splitAtIndex, forkThreshold);
                invokeAll(left, right);
            }
            else {
                FastList.Node<Agent> currentNode = node;
                for (int i = nNodes; i > 0; --i) {
                    currentNode = currentNode.getNext();
                    currentNode.getValue().execute();
                }
            }
        }
    }

    private void updateEnvironment() {

        final CountDownLatch doneSignal = new CountDownLatch(2);

        // distribute messages
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Collection<Command> messageCommands = commandListMap.get(CommandType.MESSAGE);
                if (messageCommands.size() > 0)
                    LOGGER.debug("{}: Delivering {} Messages", ParallelizedSimulation.this, messageCommands.size());
                for (Command command : messageCommands) {
                    command.execute();
                }

                postOffice.deliverOrDiscard(getAgents());

                doneSignal.countDown();
            }
        });

        // update kdtree
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Collection<Command> removeCommands = commandListMap.get(CommandType.AGENT_REMOVE);
                if (removeCommands.size() > 0)
                    LOGGER.debug("{}: Removing {} Agents", ParallelizedSimulation.this, removeCommands.size());
                for (Command command : removeCommands) {
                    command.execute();
                }

                for (Agent agent : getAgents()) {
                    final PolarPoint motion = agent.getMotionVector();
                    getSpace().moveObject(agent, sum(agent.getCoordinates(), motion.toCartesian()));
                }

                Collection<Command> addCommands = commandListMap.get(CommandType.AGENT_ADD);
                if (addCommands.size() > 0)
                    LOGGER.debug("{}: Adding {} Agents", ParallelizedSimulation.this, addCommands.size());
                for (Command command : addCommands) {
                    command.execute();
                }

                space.updateTopo();

                doneSignal.countDown();
            }
        });

        try {
            doneSignal.await();
        } catch (InterruptedException ie) {
            LOGGER.error("Error awaiting the the threads to finish their task in Simulation#updateEnvironment", ie);
        }

        commandListMap.clear();
    }

    private void notifyStep() {
        listenerSupport.notifyListeners(new Functor<SimulationListener>() {

            @Override
            public void update(SimulationListener listener) {
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
    public void deliverMessage(final ACLMessage message) {
        checkNotNull(message);
        commandListMap.put(CommandType.MESSAGE,
                new Command() {
                    @Override
                    public void execute() {
                        postOffice.addMessage(message);
                    }
                });
    }

    /**
     * Creates a new {@code Simulation} and calls {@link #step()} until the {@code stopTrigger} returns {@code true}
     * @param scenario the {@code Scenario} used to initialize this Simulation
     * @param stopTrigger the {@code Predicate} which will be asked after each simulation step if the simulation should stop.
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
