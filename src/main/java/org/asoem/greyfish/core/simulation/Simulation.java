package org.asoem.greyfish.core.simulation;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import javolution.util.FastList;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.PostOffice;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.individual.*;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.Location2DInterface;
import org.asoem.greyfish.core.space.Object2DInterface;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.lang.Command;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.ListenerSupport;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.*;
import static org.asoem.greyfish.core.io.GreyfishLogger.debug;
import static org.asoem.greyfish.core.io.GreyfishLogger.isDebugEnabled;
import static org.asoem.greyfish.core.simulation.Simulation.CommandType.*;
import static org.asoem.greyfish.core.space.Location2D.at;

public class Simulation implements Runnable {

    private final Map<Population, Prototype> prototypeMap = Maps.newHashMap();
    private final PostOffice postOffice = PostOffice.newInstance();

    public static Simulation newSimulation(final Scenario scenario) {
        return new Simulation(scenario);
    }

    public int getPrototypesCount() {
        return this.prototypeMap.size();
    }

    public Iterable<Object2DInterface> findObjects(Location2DInterface location, double radius) {
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
        MOVEMENT,
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

        if (GreyfishLogger.isTraceEnabled())
            listenerSupport.addListener(new SimulationListener() {

                @Override
                public void simulationStep(Simulation source) {
                    GreyfishLogger.trace("End of simulation step " + source.getSteps());
                }
            });
    }

    private void initialize() {
        this.space = new TiledSpace(scenario.getSpace());

        prototypeMap.clear();
        try {
            objectPool.clear();
        } catch (Exception e) {
            GreyfishLogger.error("Error clearing prototype pool", e);
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

    private void addAgent(Agent individual, Location2DInterface location) {
        checkAgent(individual);
        if (isDebugEnabled()) debug("Adding Agent to " + this + ": " + individual);
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
            GreyfishLogger.error("Error in prototype pool", e);
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
    public void createAgent(final Population population, final Location2DInterface location, final Genome genome) {
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
            GreyfishLogger.fatal("Error using objectPool", e);
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
            GreyfishLogger.debug("Tread interrupted!");
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
        updateEnvironment();
        processAgents();

        ++steps;
        notifyStep();
    }

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    private void processAgents() {
        if (isDebugEnabled()) debug("==== " + this + ": processing " + individuals.size() + " Agents");

        if ((individuals.size() < 100) || (Runtime.getRuntime().availableProcessors() == 1)) {
            processAgents(individuals.head(), individuals.tail());
        }
        else {
            if (isDebugEnabled()) debug("Splitting execution in two threads");
            FastList.Node<Agent> node = individuals.head();
            int middleIndex = individuals.size() >> 1;
            do node = node.getNext(); while (--middleIndex != 0);
            final FastList.Node<Agent> middleNode = node;
            final CountDownLatch doneSignal = new CountDownLatch(2);
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (isDebugEnabled()) debug("Thread1 starts");
                    processAgents(individuals.head(), middleNode);
                    doneSignal.countDown();
                    if (isDebugEnabled()) debug("Thread1 done");
                }
            });
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (isDebugEnabled()) debug("Thread2 starts");
                    processAgents(middleNode.getPrevious(), individuals.tail());
                    doneSignal.countDown();
                    if (isDebugEnabled()) debug("Thread2 done");
                }
            });
            try {
                doneSignal.await(); // wait for all to finish
                if (isDebugEnabled()) debug("All threads done");
            } catch (InterruptedException ie) {
                GreyfishLogger.error("Error awaiting the the threads to finish their task in Simulation#processAgents", ie);
            }
        }
    }

    private void processAgents(FastList.Node<Agent> node, FastList.Node<Agent> endNode) {
        for (;(node = node.getNext()) != endNode;) {
            node.getValue().execute();
        }
    }

    private void updateEnvironment() {
        if (isDebugEnabled()) debug("==== " + this + ": processing " + commanListMap.size() + " update-commands");

        final CountDownLatch doneSignal = new CountDownLatch(2);

        // distribute messages
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (isDebugEnabled()) debug("Thread1 starts");
                for (Command command : commanListMap.get(MESSAGE)) {
                    command.execute();
                }

                postOffice.deliverOrDiscard(getAgents());

                doneSignal.countDown();
                if (isDebugEnabled()) debug("Thread1 done");
            }
        });

        // update kdtree
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (isDebugEnabled()) debug("Thread2 starts");

                for (Command command : commanListMap.get(AGENT_REMOVE)) {
                    command.execute();
                }

                for (Command command : commanListMap.get(AGENT_ADD)) {
                    command.execute();
                }

                for (Command command : commanListMap.get(MOVEMENT)) {
                    command.execute();
                }

                space.updateTopo();

                doneSignal.countDown();
                if (isDebugEnabled()) debug("Thread2 done");
            }
        });
        try {
            doneSignal.await(); // wait for all to finish
            if (isDebugEnabled()) debug("All threads done");
        } catch (InterruptedException ie) {
            GreyfishLogger.error("Error awaiting the the threads to finish their task in Simulation#updateEnvironment", ie);
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
        return "Sim[" + getTitle() + "] running '" + scenario + "'";
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

    public void translate(final Agent agent, final double distance) {
        commanListMap.put(MOVEMENT,
                new Command() {
                    @Override
                    public void execute() {
                        final double x_add = distance * Math.cos(agent.getOrientation());
                        final double y_add = distance * Math.sin(agent.getOrientation());

                        final double x_res = agent.getX() + x_add;
                        final double y_res = agent.getY() + y_add;
                        Location2D newLocation = Location2D.at(x_res, y_res);
                        getSpace().moveObject(agent, newLocation);
                    }
                });
    }

    public void rotate(final Agent agent, final double alpha) {
        commanListMap.put(MOVEMENT,
                new Command() {
                    @Override
                    public void execute() {
                        agent.setOrientation(alpha);
                    }
                });
    }
}
