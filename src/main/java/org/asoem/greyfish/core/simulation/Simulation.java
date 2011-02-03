package org.asoem.greyfish.core.simulation;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import javolution.util.FastList;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.acl.PostOffice;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.IndividualInterface;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.individual.Prototype;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.space.Location2DInterface;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.lang.Command;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.FastLists;
import org.asoem.greyfish.utils.ListenerSupport;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.core.space.Location2D.at;

public class Simulation implements Runnable {

    private final Map<Population, Prototype> prototypeMap = Maps.newHashMap();
    private final PostOffice postOffice = PostOffice.newInstance();

    public static Simulation newSimulation(final Scenario scenario) {
        return new Simulation(scenario);
    }

    /**
     *
     * @return the PostOffice instance for this Simulation. Guarantied to be not {@code null}
     */
    public PostOffice getPostOffice() {
        return postOffice;
    }

    public int getPrototypesCount() {
        return this.prototypeMap.size();
    }

    public enum Speed {
        SLOW(20),
        MEDIUM(10),
        FAST(5),
        FASTEST(0);

        private double sleepMillies;

        private Speed(double sleepMillies) {
            this.sleepMillies = sleepMillies;
        }

        public double getTps() {
            return sleepMillies;
        }
    }

    private final FastList<Command> commandList	= FastList.newInstance();

    public final boolean enqueAfterStepCommand(Command value) {
        return commandList.add(value);
    }

    private final FastList<IndividualInterface> individuals = FastList.newInstance();

    private final ListenerSupport<SimulationListener> listenerSupport = ListenerSupport.newInstance();

    private final KeyedObjectPool objectPool = new StackKeyedObjectPool(
            new BaseKeyedPoolableObjectFactory() {

                @Override
                public Object makeObject(Object key) throws Exception {
                    checkNotNull(key);
                    checkArgument(key instanceof Population);
                    final Prototype prototype = prototypeMap.get(Population.class.cast(key));
                    final IndividualInterface clone = prototype.deepClone(IndividualInterface.class);
                    clone.freeze();
                    return clone;
                }
            },
            10000, 100);

    //	private final HashMap<Population, PopulationLog> populationLogs = new HashMap<Population, PopulationLog>();

    private Speed currentSpeed;

    private boolean infinite = true;

    private int initTicks;

    private long lastExecutionTimeMillis;

    private int maxId;

    private boolean pause;

    private boolean running;

    private int runs;

    private Scenario scenario;

    private TiledSpace space;

    private int stepsToGo;

    private final AtomicInteger steps = new AtomicInteger();

    private String title = "Simulation";

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
            clone.freeze();
            assert (!prototypeMap.containsKey(clone.getPopulation())) : "Different Prototypes have the same Population";
            prototypeMap.put(clone.getPopulation(), clone);
        }

        // convert each placeholder to a concrete object
        for (IndividualInterface placeholder : scenario.getPlaceholder()) {
            final Agent clone = Agent.newInstance(placeholder, Simulation.this);
            prepareForIntegration(clone, getSteps());
            addAgent(clone, at(placeholder));
        }

        space.updateTopo();
    }

    private void prepareForIntegration(Agent individual, int timeOfBirth) {
        checkAgent(individual);
        individual.setTimeOfBirth(timeOfBirth);
        individual.setId(++maxId);
        individual.initialize(this);
    }

    /**
     * @return a copy of the list of active individuals
     */
    public synchronized FastList<IndividualInterface> getAgents() {
        return individuals; // TODO: return an unmodifiable view of this FastList
    }

    /**
     * Add offspring to this scenario, placed relative to its parent
     * @param offspring
     * @param parent
     */
    public final void addNextStep(final Agent offspring, final Agent parent) {
        addNextStep(offspring, parent.getAnchorPoint());
    }

    /**
     * Add individual to this simulation, placed at given location
     * @param individual
     * @param location
     */
    public final synchronized void addNextStep(final Agent individual, final Location2DInterface location) {
        prepareForIntegration(individual, getSteps() + 1);

        enqueAfterStepCommand(new Command() {
            @Override
            public void execute() {
                addAgent(individual, at(location));
            }
        });
    }

    private void addAgent(Agent individual, Location2DInterface location) {
        individuals.add(individual);
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
    public synchronized void removeAgent(final IndividualInterface individual) {
        checkArgument(Agent.class.isInstance(individual));
        /*
           * TODO: removal could be implemented more efficiently.
           * e.g. by marking agents and removal during a single iteration over all
           */
        enqueAfterStepCommand(new Command() {
            @Override
            public void execute() {
                space.removeOccupant(individual);
                individuals.remove(individual);
                returnClone(Agent.class.cast(individual));
            }
        });
    }

    private void returnClone(final Agent individual) {
        checkAgent(individual);
        try {
            objectPool.returnObject(individual.getPopulation(), individual.getDelegate());
        } catch (Exception e) {
            GreyfishLogger.error("Error in prototype pool", e);
        }
    }

    public synchronized int agentCount() {
        return individuals.size();
    }

    public void addSimulationListener(SimulationListener listener) {
        listenerSupport.addListener(listener);
    }

    public void removeSimulationListener(SimulationListener listener) {
        listenerSupport.removeListener(listener);
    }

    public int generateAgentID() {
        return ++maxId;
    }

    /**
     * @return a deepClone of the prototype for given population registered by the underlying scenario of this simulation
     * @throws Exception if the clone could not be created
     */
    public IndividualInterface createClone(final Population population) {
        Preconditions.checkNotNull(population);
        Preconditions.checkState(prototypeMap.containsKey(population));
        try {
            return Agent.newInstance(IndividualInterface.class.cast(objectPool.borrowObject(population)), this);
        } catch (Exception e) {
            GreyfishLogger.fatal("Error using objectPool", e);
            throw new AssertionError(e);
        }
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
        return steps.get();
    }

    public synchronized void pause() {
        if (running) {
            pause = true;
        }
    }

    public synchronized void reset() {
        stop();

        clearAgentList();

        steps.set(0);

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
        clearAgentList();
    }

    private void clearAgentList() {
        individuals.clear();
        // TODO: recycle?
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
     * Increase the time by one and return its (increased) to
     * @return
     */
    public synchronized void step() {
        processAgents();
        processAfterStepCommands();
        space.updateTopo();
        commandList.clear();
        steps.incrementAndGet();
        notifyStep();
    }

    private void processAgents() {
        FastLists.foreach(individuals, new Functor<IndividualInterface>() {
            @Override public void update(IndividualInterface individual) {
                individual.execute();
            }
        });
    }

    private void processAfterStepCommands() {
        FastLists.foreach(commandList, new Functor<Command>() {
            @Override public void update(Command c) {
                c.execute();
            }
        });
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
        return getTitle() + " (SC:" + scenario.getName() + ")";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }


}
