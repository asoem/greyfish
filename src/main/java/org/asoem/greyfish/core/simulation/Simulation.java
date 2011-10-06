package org.asoem.greyfish.core.simulation;

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
import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.MovingObject2D;
import org.asoem.greyfish.core.space.MutableLocation2D;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.lang.Command;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.lang.HasName;
import org.asoem.greyfish.utils.Counter;
import org.asoem.greyfish.utils.ListenerSupport;
import org.asoem.greyfish.utils.PolarPoint;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.*;
import static org.asoem.greyfish.core.simulation.Simulation.CommandType.*;
import static org.asoem.greyfish.core.space.MutableLocation2D.at;

public class Simulation implements Runnable, HasName {

    private static final Logger LOGGER = LoggerFactory.getLogger(Simulation.class);
    private final Map<Population, Agent> prototypeMap = Maps.newHashMap();
    private final PostOffice postOffice = PostOffice.newInstance();
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    private final Map<Population, Counter> populationCount = Maps.newHashMap();

    @SuppressWarnings("unused")
    public enum Speed {
        SLOW(32),
        MEDIUM(24),
        FAST(16),
        FASTEST(0);

        private final double sleepMillies;

        private Speed(double sleepMillies) {
            this.sleepMillies = sleepMillies;
        }

        public double getTps() {
            return sleepMillies;
        }
    }

    public enum CommandType {
        MESSAGE,
        AGENT_REMOVE,
        AGENT_ADD
    }

    private final Multimap<CommandType, Command> commandListMap =
            Multimaps.synchronizedMultimap(HashMultimap.<CommandType, Command>create());

    private final FastList<Agent> individuals = FastList.newInstance();
    private final Collection<Agent> concurrentAgentsView = individuals.shared();

    private final ListenerSupport<SimulationListener> listenerSupport = ListenerSupport.newInstance();

    private final KeyedObjectPool objectPool = new StackKeyedObjectPool(
            new BaseKeyedPoolableObjectFactory() {

                @Override
                public Object makeObject(Object key) throws Exception {
                    checkNotNull(key);
                    checkArgument(key instanceof Population);
                    return ImmutableAgent.cloneOf(prototypeMap.get(Population.class.cast(key)));
                }
            },
            10000, 100);

    //	private final HashMap<Population, PopulationLog> populationLogs = new HashMap<Population, PopulationLog>();

    private Speed currentSpeed;

    private boolean infinite = true;

    private int initTicks;

    private long lastExecutionTimeMillis;

    private AtomicInteger maxId = new AtomicInteger();

    private boolean pause;

    private boolean running;

    private int runs;

    private final Scenario scenario;

    private TiledSpace space;

    private int stepsToGo;

    private int steps = 0;

    private String title = "untitled";

    private Simulation(final Scenario scenario) {
        checkNotNull(scenario);

        this.scenario = scenario;

        initialize();
        setSpeed(Speed.MEDIUM);

        if (LOGGER.isTraceEnabled())
            listenerSupport.addListener(new SimulationListener() {
                @Override
                public void eventFired(SimulationEvent event) {
                    LOGGER.trace("End of simulation step " + event.getSource().getSteps());
                }
            });
    }

    public static Simulation newSimulation(final Scenario scenario) {
        return new Simulation(scenario);
    }

    public int getPrototypesCount() {
        return this.prototypeMap.size();
    }

    public Iterable<MovingObject2D> findObjects(Location2D location, double radius) {
        return space.findNeighbours(location, radius);
    }

    public Iterable<Agent> getAgents(final Population population) {
        checkNotNull(population);
        return Iterables.filter(concurrentAgentsView, new Predicate<Agent>() {
            @Override
            public boolean apply(Agent agent) {
                return agent.getPopulation().equals(population);
            }
        });
    }

    private void initialize() {
        this.space = new TiledSpace(scenario.getSpace());

        prototypeMap.clear();
        try {
            objectPool.clear();
        } catch (Exception e) {
            LOGGER.error("Error clearing prototype pool", e);
            System.exit(1);
        }

        for (Agent prototype : scenario.getPrototypes()) {
            ImmutableAgent clone = ImmutableAgent.cloneOf(prototype);
            assert (!prototypeMap.containsKey(clone.getPopulation())) : "Different Prototypes have the same Population";
            prototypeMap.put(clone.getPopulation(), clone);
            populationCount.put(clone.getPopulation(), new Counter(0));
        }

        // convert each placeholder to a concrete object
        for (Agent placeholder : scenario.getPlaceholder()) {
            final Agent clone = newAgentFromPool(placeholder.getPopulation());
            addAgent(clone, at(placeholder));
        }

        space.updateTopo();
    }

    /**
     * @return a copy of the list of active individuals
     */
    public Collection<Agent> getAgents() {
        return Collections.unmodifiableCollection(concurrentAgentsView);
    }

    private void addAgent(Agent agent, Location2D location) {
        checkAgent(agent);
        LOGGER.trace("{}: Adding Agent {}" + this, agent);
        agent.prepare(this);
        concurrentAgentsView.add(agent);
        populationCount.get(agent.getPopulation()).increase();
        agent.setAnchorPoint(location);
        space.addOccupant(agent);
    }

    private void checkAgent(final Agent agent) {
        checkArgument(ImmutableAgent.class.isInstance(agent));
        checkArgument(prototypeMap.containsKey(agent.getPopulation()), "Not prototype found for " + agent);
    }

    /**
     * Remove agent from this scenario
     * @param agent
     */
    public void removeAgent(final Agent agent) {
        checkNotNull(agent);

        /*
           * TODO: removal could be implemented more efficiently.
           * e.g. by marking agents and removal during a single iteration over all
           */
        commandListMap.put(AGENT_REMOVE,
                new Command() {
                    @Override
                    public void execute() {
                        space.removeOccupant(agent);
                        concurrentAgentsView.remove(agent);
                        populationCount.get(agent.getPopulation()).decrease();
                        agent.shutDown();
                        putCloneInPool(agent);
                    }
                });
    }

    private void putCloneInPool(final Agent agent) {
        checkAgent(agent);
        try {
            objectPool.returnObject(agent.getPopulation(), agent);
        } catch (Exception e) {
            LOGGER.error("Error in prototype pool", e);
        }
    }

    public int agentCount() {
        return getAgents().size();
    }

    public int agentCount(Population population) {
        return populationCount.get(population).get();
    }

    public void addSimulationListener(SimulationListener listener) {
        listenerSupport.addListener(listener);
    }

    public void removeSimulationListener(SimulationListener listener) {
        listenerSupport.removeListener(listener);
    }

    public int generateAgentID() {
        return maxId.incrementAndGet();
    }

    /**
     * Creates a new {@link org.asoem.greyfish.core.individual.Agent} as clone of the prototype registered for given {@code population} with genome set to {@code genome}.
     * The {@link org.asoem.greyfish.core.individual.Agent} will get inserted and executed at the next step at given {@code location}.
     * @param population The {@code Population} of the {@code Prototype} the Agent will be cloned from.
     * @param location The location where the {@link org.asoem.greyfish.core.individual.Agent} will be inserted in the {@link org.asoem.greyfish.core.space.Space}.
     * @param genome The {@link org.asoem.greyfish.core.genes.ImmutableGenome} for the new {@link org.asoem.greyfish.core.individual.Agent}.
     */
    public void createAgent(final Population population, final Location2D location, final Genome genome) {
        checkNotNull(population);
        checkState(prototypeMap.containsKey(population));

        final Agent agent = newAgentFromPool(population);
        agent.injectGamete(genome);

        commandListMap.put(AGENT_ADD,
                new Command() {
                    @Override
                    public void execute() {
                        addAgent(agent, at(location));
                    }
                });
    }

    private Agent newAgentFromPool(final Population population) {
        assert population != null;
        assert prototypeMap.containsKey(population);
        try {
            return Agent.class.cast(objectPool.borrowObject(population));
        } catch (Exception e) {
            LOGGER.error("Error using objectPool", e);
            System.exit(1);
        }
        return null;
    }

    public Iterable<Agent> getPrototypes() {
        return prototypeMap.values();
    }

    public TiledSpace getSpace() {
        return space;
    }

    public int getSteps() {
        return steps;
    }

    public synchronized void pause() {
        if (running) {
            pause = true;
            listenerSupport.notifyListeners(new Functor<SimulationListener>() {
                @Override
                public void update(SimulationListener listener) {
                    listener.eventFired(new SimulationEvent(Simulation.this, SimulationEvent.Event.STOP));
                }
            });
        }
    }

    public synchronized void reset() {
        stop();

        clearExecutionLists();

        steps = 0;

        initialize();
        notifyStep(); // TODO: should fire RESET
    }

    @Override
    public void run() {
        try {
            do {
                synchronized (this) {
                    running = false;
                    listenerSupport.notifyListeners(new Functor<SimulationListener>() {
                        @Override
                        public void update(SimulationListener listener) {
                            listener.eventFired(new SimulationEvent(Simulation.this, SimulationEvent.Event.STOP));
                        }
                    });

                    wait();

                    running = true;
                    listenerSupport.notifyListeners(new Functor<SimulationListener>() {
                        @Override
                        public void update(SimulationListener listener) {
                            listener.eventFired(new SimulationEvent(Simulation.this, SimulationEvent.Event.START));
                        }
                    });
                }

                while (!infinite && runs > 0 && !pause) {
                    stepsToGo = initTicks;
                    while (stepsToGo > 0) {
                        // do the tick
                        synchronized (this) {
                            timedStep();
                            --stepsToGo;
                        }
                    }
                    synchronized (this) {
                        if (runs > 1) {
                            reset();
                        }
                        --runs;
                    }
                }

                while (infinite && ! pause)
                    synchronized (this) {
                        timedStep();
                    }

            } while(true);
        } catch (InterruptedException e) {
            LOGGER.warn("Tread interrupted!");
        }
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void setInfinite(boolean infinite) {
        this.infinite = infinite;
    }

    public Speed getSpeed() {
        return currentSpeed;
    }

    public void setSpeed(Speed speed) {
        currentSpeed = speed;
    }

    public synchronized void start() {
        if (!running) {
            pause = false;
            infinite = true;
            notify();
        }
    }

    public synchronized void stop() {
        stepsToGo = 0;
        runs = 0;
        infinite = false;
        pause = false;
        clearExecutionLists();
    }

    /**
     * Clears all lists which hold either agents or commands
     */
    private void clearExecutionLists() {
        concurrentAgentsView.clear();
        commandListMap.clear();
    }

    private void timedStep() throws InterruptedException {
        if (currentSpeed.getTps() > 0) {
            do {
                TimeUnit.MILLISECONDS.timedWait(this, 2);
            } while (System.currentTimeMillis() - lastExecutionTimeMillis < currentSpeed.getTps());
            lastExecutionTimeMillis = System.currentTimeMillis();
        }
        step();
    }

    /**
     * Proceed on step cycle and execute all agents & commands
     */
    public synchronized void step() {
        LOGGER.trace("{}: Entering step {}", this, steps);
        updateEnvironment();
        processAgents();

        ++steps;
        notifyStep();
    }

    private void processAgents() {
        LOGGER.debug("{}: processing {} Agents", this, individuals.size());
        if (individuals.size() > 0)
            forkJoinPool.invoke(new ProcessAgentsForked(individuals.head(), individuals.size(), Math.max(individuals.size(), 1000) / 2));
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
                Collection<Command> messageCommands = commandListMap.get(MESSAGE);
                if (messageCommands.size() > 0)
                    LOGGER.debug("{}: Delivering {} Messages", Simulation.this, messageCommands.size());
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
                Collection<Command> removeCommands = commandListMap.get(AGENT_REMOVE);
                if (removeCommands.size() > 0)
                    LOGGER.debug("{}: Removing {} Agents", Simulation.this, removeCommands.size());
                for (Command command : removeCommands) {
                    command.execute();
                }

                for (Agent agent : getAgents()) {
                    final PolarPoint motion = agent.getMotionVector();
                    getSpace().moveObject(agent, MutableLocation2D.sum(agent, motion.toCartesian()));
                }

                Collection<Command> addCommands = commandListMap.get(AGENT_ADD);
                if (addCommands.size() > 0)
                    LOGGER.debug("{}: Adding {} Agents", Simulation.this, addCommands.size());
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
                listener.eventFired(new SimulationEvent(Simulation.this, SimulationEvent.Event.STEP));
            }
        });
    }

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
    public boolean hasName(String s) {
        return Objects.equal(title, s);
    }

    public void setName(String name) {
        this.title = name;
    }

    public void deliverMessage(final ACLMessage message) {
        commandListMap.put(MESSAGE,
                new Command() {
                    @Override
                    public void execute() {
                        postOffice.addMessage(message);
                    }
                });
    }
}
