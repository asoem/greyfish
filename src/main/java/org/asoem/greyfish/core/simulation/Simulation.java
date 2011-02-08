package org.asoem.greyfish.core.simulation;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import javolution.lang.MathLib;
import javolution.util.FastList;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.acl.*;
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.*;
import static org.asoem.greyfish.core.space.Location2D.at;

public class Simulation implements Runnable, ACLMessageTransmitter, ACLMessageReceiver {

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

    public Iterable<Object2DInterface> findObjects(Location2DInterface location, double radius) {
        return space.findNeighbours(location, radius);
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

    private final FastList<Agent> individuals = FastList.newInstance();

    private final ListenerSupport<SimulationListener> listenerSupport = ListenerSupport.newInstance();

    private final KeyedObjectPool objectPool = new StackKeyedObjectPool(
            new BaseKeyedPoolableObjectFactory() {

                @Override
                public Object makeObject(Object key) throws Exception {
                    checkNotNull(key);
                    checkArgument(key instanceof Population);
                    final Prototype prototype = prototypeMap.get(Population.class.cast(key));
                    final Individual clone = prototype.getIndividual().deepClone(Individual.class);
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
            assert (!prototypeMap.containsKey(clone.getPopulation())) : "Different Prototypes have the same Population";
            prototypeMap.put(clone.getPopulation(), clone);
        }

        // convert each placeholder to a concrete object
        for (Placeholder placeholder : scenario.getPlaceholder()) {
            final Agent clone = newAgentFromPool(placeholder.getPopulation());
            prepareForIntegration(clone);
            addAgent(clone, at(placeholder));
        }

        space.updateTopo();
    }

    private void prepareForIntegration(Agent individual) {
        checkAgent(individual);
    }

    /**
     * @return a copy of the list of active individuals
     */
    public synchronized FastList<Agent> getAgents() {
        return individuals; // TODO: return an unmodifiable view of this FastList
    }

    /**
     * Add individual to this simulation, placed at given location
     * @param individual
     * @param location
     */
    private synchronized void addNextStep(final Agent individual, final Location2DInterface location) {
        prepareForIntegration(individual);

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
                individuals.remove(Agent.class.cast(individual));
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
     * Creates a new {@code Agent} as clone of the prototype registered for given {@code population} with genome set to {@code genome}.
     * The {@code Agent} will get inserted and executed at the next step at given {@code location}.
     * @param population The {@code Population} of the {@code Prototype} the Agent will be cloned from.
     * @param location The location where the {@Agent} will be inserted in the {@Space}.
     * @param genome The {@code Genome} for the new {@code Agent}.
     */
    public void createAgent(final Population population, final Location2DInterface location, final Genome genome) {
        checkNotNull(population);
        checkState(prototypeMap.containsKey(population));

        Agent agent = newAgentFromPool(population);
        agent.setGenome(genome);
        addNextStep(agent, location);
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
        for (FastList.Node<Agent> n = individuals.head(), end = individuals.tail(); (n = n.getNext()) != end;) {
            n.getValue().execute();
        }
    }

    private void processAfterStepCommands() {
        for (FastList.Node<Command> n = commandList.head(), end = commandList.tail(); (n = n.getNext()) != end;) {
            n.getValue().execute();
        }
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

    @Override
    public void deliverMessage(ACLMessage message) {
        postOffice.addMessage(message);
    }

    @Override
    public List<ACLMessage> pollMessages(MessageTemplate messageTemplate) {
        return postOffice.pollMessages(messageTemplate);
    }

    public List<ACLMessage> pollMessages(int receiverId, MessageTemplate messageTemplate) {
        return postOffice.pollMessages(receiverId, messageTemplate);
    }

    public void translate(final Agent agent, double distance) {

        final double x_add = distance * Math.cos(agent.getOrientation());
        final double y_add = distance * Math.sin(agent.getOrientation());

        final double x_res = agent.getX() + x_add;
        final double y_res = agent.getY() + y_add;

        enqueAfterStepCommand(new Command() {
            @Override
            public void execute() {
                Location2D newLocation = Location2D.at(x_res, y_res);
                getSpace().moveObject(agent, newLocation);

                if (!agent.getAnchorPoint().equals(newLocation)) { // collision
                    rotate(agent, MathLib.PI);
                }
            }
        });
    }

    public void rotate(final Agent agent, double alpha) {
        agent.setOrientation(alpha);
    }
}
