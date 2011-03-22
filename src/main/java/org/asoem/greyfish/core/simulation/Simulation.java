package org.asoem.greyfish.core.simulation;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import javolution.util.FastList;
import jsr166y.ForkJoinPool;
import jsr166y.RecursiveAction;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.PostOffice;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.individual.*;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.MovingObject2D;
import org.asoem.greyfish.core.space.MutableLocation2D;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.lang.Command;
import org.asoem.greyfish.lang.Functor;
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
import static org.asoem.greyfish.core.io.GreyfishLogger.SIMULATION_LOGGER;
import static org.asoem.greyfish.core.simulation.Simulation.CommandType.*;
import static org.asoem.greyfish.core.space.MutableLocation2D.at;

public class Simulation implements Runnable {

    private final Map<Population, Prototype> prototypeMap = Maps.newHashMap();
    private final PostOffice postOffice = PostOffice.newInstance();
    private final static int FORK_JOIN_THRESHOLD = 500;
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static Simulation newSimulation(final Scenario scenario) {
        return new Simulation(scenario);
    }

    public int getPrototypesCount() {
        return this.prototypeMap.size();
    }

    public Iterable<MovingObject2D> findObjects(Location2D location, double radius) {
        return space.findNeighbours(location, radius);
    }

    @SuppressWarnings("unused")
    public enum Speed {
        SLOW(20),
        MEDIUM(10),
        FAST(5),
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

    private final Multimap<CommandType, Command> commanListMap =
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
                    final Prototype prototype = prototypeMap.get(Population.class.cast(key));
                    return prototype.getIndividual().deepClone(Individual.class);
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
        Preconditions.checkNotNull(scenario);

        this.scenario = scenario;

        initialize();
        setSpeed(Speed.MEDIUM);

        if (SIMULATION_LOGGER.isTraceEnabled())
            listenerSupport.addListener(new SimulationListener() {

                @Override
                public void simulationStep(Simulation source) {
                    SIMULATION_LOGGER.trace("End of simulation step " + source.getSteps());
                }
            });
    }

    private void initialize() {
        this.space = new TiledSpace(scenario.getSpace());

        prototypeMap.clear();
        try {
            objectPool.clear();
        } catch (Exception e) {
            SIMULATION_LOGGER.error("Error clearing prototype pool", e);
            System.exit(1);
        }

        for (Prototype prototype : scenario.getPrototypes()) {
            Prototype clone = prototype.deepClone(Prototype.class);
            assert (!prototypeMap.containsKey(clone.getPopulation())) : "Different Prototypes have the same Population";
            prototypeMap.put(clone.getPopulation(), clone);
        }

        // convert each placeholder to a concrete object
        for (Placeholder placeholder : scenario.getPlaceholder()) {
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

    private void addAgent(Agent individual, Location2D location) {
        checkAgent(individual);
        if (SIMULATION_LOGGER.isDebugEnabled())
            SIMULATION_LOGGER.debug("Adding Agent to " + this + ": " + individual);
        individual.initialize(this);
        concurrentAgentsView.add(individual);
        individual.setAnchorPoint(location);
        space.addOccupant(individual);
    }

    private void checkAgent(final Agent individual) {
        Preconditions.checkNotNull(individual);
        Preconditions.checkArgument(prototypeMap.containsKey(individual.getPopulation()), "Not prototype found for " + individual);
    }

    /**
     * Remove individual from this scenario
     * @param individual
     */
    public void removeAgent(final IndividualInterface individual) {
        checkArgument(Agent.class.isInstance(individual));
        /*
           * TODO: removal could be implemented more efficiently.
           * e.g. by marking agents and removal during a single iteration over all
           */
        commanListMap.put(AGENT_REMOVE,
                new Command() {
                    @Override
                    public void execute() {
                        space.removeOccupant(individual);
                        concurrentAgentsView.remove(Agent.class.cast(individual));
                        returnClone(Agent.class.cast(individual));
                    }
                });
    }

    private void returnClone(final Agent individual) {
        checkAgent(individual);
        try {
            objectPool.returnObject(individual.getPopulation(), individual.getIndividual());
        } catch (Exception e) {
            SIMULATION_LOGGER.error("Error in prototype pool", e);
        }
    }

    public int agentCount() {
        return getAgents().size();
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
     * Creates a new {@code Agent} as clone of the prototype registered for given {@code population} with genome set to {@code genome}.
     * The {@code Agent} will get inserted and executed at the next step at given {@code location}.
     * @param population The {@code Population} of the {@code Prototype} the Agent will be cloned from.
     * @param location The location where the {@Agent} will be inserted in the {@Space}.
     * @param genome The {@code Genome} for the new {@code Agent}.
     */
    public void createAgent(final Population population, final Location2D location, final Genome genome) {
        checkNotNull(population);
        checkState(prototypeMap.containsKey(population));

        final Agent agent = newAgentFromPool(population);
        agent.setGenome(genome);

        commanListMap.put(AGENT_ADD,
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
            return Agent.newInstance(Individual.class.cast(objectPool.borrowObject(population)), this);
        } catch (Exception e) {
            SIMULATION_LOGGER.error("Error using objectPool", e);
            System.exit(1);
        }
        return null;
    }

    public Iterable<Prototype> getPrototypes() {
        return prototypeMap.values();
    }

    public TiledSpace getSpace() {
        return space;
    }

    //	public Collection<PopulationLog> getPopulationLogs() {
    //		return populationLogs.values();
    //	}

    public synchronized int getStepsToGo() {
        return stepsToGo;
    }

    public int getSteps() {
        return steps;
    }

    public synchronized void pause() {
        if (running) {
            pause = true;
        }
    }

    public synchronized void reset() {
        stop();

        clearExecutionLists();

        steps = 0;

        initialize();
        notifyStep();
    }

    @Override
    public void run() {
        try {
            do {
                synchronized (this) {
                    running = false;
                    wait();
                    running = true;
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
            SIMULATION_LOGGER.warn("Tread interrupted!");
        }
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized boolean isInfinite() {
        return infinite;
    }

    public synchronized void setInfinite(boolean infinite) {
        this.infinite = infinite;
    }

    public synchronized int getRuns() {
        return runs;
    }

    public synchronized void setRuns(int runs) {
        this.runs = runs;
    }

    public Speed getSpeed() {
        return currentSpeed;
    }

    public void setSpeed(Speed speed) {
        currentSpeed = speed;
    }

    public synchronized void setStepsToGo(int ticks) {
        this.stepsToGo = ticks;
    }

    public synchronized void start() {
        if (!running) {
            pause = false;
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
        commanListMap.clear();
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
        SIMULATION_LOGGER.debug("%s: Step #%d", this, steps);
        updateEnvironment();
        processAgents();

        ++steps;
        notifyStep();
    }

    private void processAgents() {
        SIMULATION_LOGGER.debug("{}: processing {} Agents", this, individuals.size());
        forkJoinPool.invoke(new ProcessAgentsForked(individuals.head(), individuals.size()));
    }

    private class ProcessAgentsForked extends RecursiveAction {

        final FastList.Node<Agent> node;
        final int nNodes;

        private ProcessAgentsForked(FastList.Node<Agent> node, int nNodes) {
            assert node != null;
            assert nNodes > 0;

            this.node = node;
            this.nNodes = nNodes;
        }

        @Override
        protected void compute() {
            if (nNodes > FORK_JOIN_THRESHOLD) {
                // split list
                final int splitAtIndex = nNodes / 2;
                FastList.Node<Agent> iterNode = node;

                for (int i = splitAtIndex; i-- >= 0;) {
                    iterNode = iterNode.getNext();
                }

                final FastList.Node<Agent> splitNode = node;

                // fork
                final ProcessAgentsForked left = new ProcessAgentsForked(node, splitAtIndex);
                final ProcessAgentsForked right = new ProcessAgentsForked(splitNode.getPrevious(), nNodes - splitAtIndex);
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
        SIMULATION_LOGGER.debug("{}: processing {} Update-Commands.", this, commanListMap.size());

        final CountDownLatch doneSignal = new CountDownLatch(2);

        // distribute messages
        executorService.execute(new Runnable() {
            @Override
            public void run() {

                for (Command command : commanListMap.get(MESSAGE)) {
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
                for (Command command : commanListMap.get(AGENT_REMOVE)) {
                    command.execute();
                }

                for (Agent agent : getAgents()) {
                    final PolarPoint motion = agent.getMotionVector();
                    getSpace().moveObject(agent, MutableLocation2D.sum(agent, motion.toCartesian()));
                }

                for (Command command : commanListMap.get(AGENT_ADD)) {
                    command.execute();
                }

                space.updateTopo();

                doneSignal.countDown();
            }
        });
        try {
            doneSignal.await();
        } catch (InterruptedException ie) {
            SIMULATION_LOGGER.error("Error awaiting the the threads to finish their task in Simulation#updateEnvironment", ie);
        }

        commanListMap.clear();
    }

    private void notifyStep() {
        listenerSupport.notifyListeners(new Functor<SimulationListener>() {

            @Override
            public void update(SimulationListener listener) {
                listener.simulationStep(Simulation.this);
            }
        });
    }

    public Scenario getScenario() {
        return scenario;
    }

    @Override
    public String toString() {
        return "Simulation['" + getTitle() + "'] for " + scenario;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public void deliverMessage(final ACLMessage message) {
        commanListMap.put(MESSAGE,
                new Command() {
                    @Override
                    public void execute() {
                        postOffice.addMessage(message);
                    }
                });
    }
}
