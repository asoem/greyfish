package org.asoem.greyfish.core.simulation;

import com.google.common.base.Preconditions;
import javolution.util.FastList;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.individual.SimulationObject;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.Placeholder;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.lang.Command;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.FastLists;
import org.asoem.greyfish.utils.ListenerSupport;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Simulation implements Runnable {

    private final Map<Population, Individual> prototypeMap = new HashMap<Population, Individual>();

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

    private final FastList<Command> commandList	= new FastList<Command>();

    public final boolean enqueAfterStepCommand(Command value) {
        return commandList.add(value);
    }

    private final FastList<SimulationObject> individuals = new FastList<SimulationObject>();

    private final ListenerSupport<SimulationListener> listenerSupport = new ListenerSupport<SimulationListener>();

    private final KeyedObjectPool objectPool = new StackKeyedObjectPool(
            new BaseKeyedPoolableObjectFactory() {

                @Override
                public Object makeObject(Object key) throws Exception {
                    checkNotNull(key);
                    checkArgument(key instanceof Population);
                    Individual prototype = prototypeMap.get(key);
                    Individual clone = (Individual) prototype.deepClone();
                    clone.freeze();
                    return clone;
                }

                @Override
                public void activateObject(Object key, Object obj) throws Exception {
                    Individual individual = (Individual)obj;
                    individual.initialize(Simulation.this);
                }

                @Override
                public void passivateObject(Object key, Object obj) throws Exception {
                    Individual individual = (Individual)obj;
                    individual.passivate();
                    //			individual.setLocation(null); // TODO: move this to somewhere else (as part of death()?)
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

    public Simulation(final Scenario scenario) {
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
        this.space = new TiledSpace(scenario.getPrototypeSpace());

        prototypeMap.clear();
        try {
            objectPool.clear();
        } catch (Exception e) {
            GreyfishLogger.error("Error clearing prototype pool", e);
            System.exit(1);
        }

        for (SimulationObject prototype : scenario.getPrototypes()) {
            Individual clone = Individual.class.cast(prototype.deepClone());
            clone.finishAssembly(); // TODO: can be considered as a workaround. Prototype might get treated differently in future versions
            assert (!prototypeMap.containsKey(clone.getPopulation())) : "Different Prototypes have the same Population";
            prototypeMap.put(clone.getPopulation(), clone);
        }

        // convert each placeholder to a concrete object
        for (Placeholder placeholder : scenario.getPlaceholder()) {
            SimulationObject clone = placeholder.createReplacement();
            clone.freeze();
            prepareForIntegration(clone, getSteps());
            addIndividual(clone, new Location2D(placeholder.getAnchorPoint()));
        }

        space.updateTopo();
    }

    private void prepareForIntegration(SimulationObject individual, int timeOfBirth) {
        checkIndividual(individual);
        individual.setId(++maxId);
        individual.setTimeOfBirth(timeOfBirth);
        individual.initialize(this);
    }

    /**
     * @return a copy of the list of active individuals
     */
    public synchronized FastList<SimulationObject> getIndividuals() {
        return individuals;
    }

    /**
     * Add offspring to this scenario, placed relative to its parent
     * @param offspring
     * @param parent
     */
    public final void addNextStep(final Individual offspring, final Individual parent) {
        addNextStep(offspring, parent.getAnchorPoint());
    }

    /**
     * Add individual to this simulation, placed at given location
     * @param individual
     * @param location
     */
    public final synchronized void addNextStep(final Individual individual, final Location2D location) {
        Preconditions.checkState(individual.getState() == Individual.State.ACTIVE_CLONE);
        prepareForIntegration(individual, getSteps() + 1);

        enqueAfterStepCommand(new Command() {
            @Override
            public void execute() {
                addIndividual(individual, new Location2D(location));
            }
        });
    }

    private void addIndividual(SimulationObject individual, Location2D location) {
                individuals.add(individual);
                space.add(individual, new Location2D(location));
    }

    private void checkIndividual(final SimulationObject individual) {
        Preconditions.checkNotNull(individual);
        Preconditions.checkArgument(prototypeMap.containsKey(individual.getPopulation()), "Not prototype found for " + individual);
        Preconditions.checkArgument(individual.isFrozen(), "Indivdual is not frozen");
    }

    /**
     * Remove individual from this scenario
     * @param individual
     */
    public synchronized void removeIndividual(final Individual individual) {
        /*
           * TODO: removal could be implemented more efficiently.
           * e.g. by marking agents and removal during a single iteration over all
           */
        enqueAfterStepCommand(new Command() {
            @Override
            public void execute() {
                space.removeOccupant(individual);
                individuals.remove(individual);
                returnClone(individual);
            }
        });
    }

    private void returnClone(final Individual individual) {
        checkIndividual(individual);
        try {
            objectPool.returnObject(individual.getPopulation(), individual);
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

    public int generateIndividualID() {
        return ++maxId;
    }

    /**
     * @return a deepClone of the prototype for given population registered by the underlying scenario of this simulation
     * @throws Exception if the clone could not be created
     */
    public Individual createClone(final Population population) {
        Preconditions.checkNotNull(population);
        Preconditions.checkState(prototypeMap.containsKey(population));
        try {
            return (Individual) objectPool.borrowObject(population);
        } catch (Exception e) {
            GreyfishLogger.fatal("Error using objectPool", e);
            throw new AssertionError(e);
        }
    }

    public Collection<Individual> getPrototypes() {
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
        FastLists.foreach(individuals, new Functor<SimulationObject>() {
            @Override public void update(SimulationObject individual) {
                individual.execute(Simulation.this);
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
