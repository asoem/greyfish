package org.asoem.greyfish.core.simulation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javolution.util.FastList;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.Placeholder;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.lang.Command;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.DeepClonable;
import org.asoem.greyfish.utils.FastLists;
import org.asoem.greyfish.utils.ListenerSupport;
import org.asoem.greyfish.utils.RandomUtils;

import com.google.common.base.Preconditions;

public class Simulation implements Runnable {

	/**
	 * Key has type {@code Population} and Object type {@code Individual}
	 */
	private final KeyedPoolableObjectFactory factory = new BaseKeyedPoolableObjectFactory() {

		@Override
		public Object makeObject(Object key) throws Exception {
			return prototypeMap.get(key).deepClone();
		}

		public void activateObject(Object key, Object obj) throws Exception {
			Individual individual = (Individual)obj;
			individual.activate(Simulation.this);
		};

		@Override
		public void passivateObject(Object key, Object obj) throws Exception {
			Individual individual = (Individual)obj;
			individual.passivate();
			//			individual.setLocation(null); // TODO: move this to somewhere else (as part of death()?)
		}
	};

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

	private final FastList<Individual> individuals = new FastList<Individual>();

	private final ListenerSupport<SimulationListener> listenerSupport = new ListenerSupport<SimulationListener>();

	private final KeyedObjectPool objectPool = new StackKeyedObjectPool(factory, 10000, 100);

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

	/**
	 * 
	 */
	private void initialize() {
		final TiledSpace pSpace = scenario.getPrototypeSpace();
		this.space = new TiledSpace(pSpace);

		prototypeMap.clear();
		try {
			objectPool.clear();
		} catch (Exception e) {
			GreyfishLogger.error("Error clearing prototype pool", e);
		}

		for (DeepClonable prototype : scenario.getPrototypes()) {
			Individual clone = (Individual) prototype.deepClone(); // TODO: unchecked cast
			clone.finishAssembly(); // TODO: can be considered as a workaround. Prototype might get treated differently in future versions
			prototypeMap.put(clone.getPopulation(), clone);
		}

		// convert each placeholder to a concrete object
		for (Placeholder placeholder : scenario.getPlaceholder()) {
			Individual clone = createClone(((Individual) placeholder.getPrototype()).getPopulation());
			addIndividual(clone, placeholder.getAnchorPoint());
		}

		// Add a log for each population
		//		for (Individual individual : getPrototypes()) {
		//			populationLogs.put(individual.getPopulation(), new PopulationLog(this, individual));
		//		}
	}

	/**
	 * @return a copy of the list of active individuals
	 */
	public synchronized FastList<Individual> getIndividuals() {
		return individuals;
	}

	/**
	 * Add individual to this simulation, placed at a random location
	 * @param individual
	 */
	public final void addIndividual(final Individual individual) {
		addIndividual(individual, new Location2D(
				RandomUtils.nextFloat(0, space.getWidth()),
				RandomUtils.nextFloat(0, space.getHeight())));
	}

	/**
	 * Add offspring to this scenario, placed relative to its parent
	 * @param offspring
	 * @param parent
	 */
	public final void addIndividual(final Individual offspring, final Individual parent) {
		addIndividual(offspring, parent.getAnchorPoint());
	}

	/**
	 * Add individual to this simulation, placed at given location
	 * @param individual
	 * @param location
	 */
	public final synchronized void addIndividual(final Individual individual, final Location2D location) {
		checkIndividual(individual);
		Preconditions.checkState(individual.getState() == Individual.State.ACTIVE_CLONE);

		individual.setId(++maxId);
		individual.setTimeOfBirth(getSteps()+1);

		enqueAfterStepCommand(new Command() {		
			@Override
			public void execute() {
				individuals.add(individual);
				space.add(individual, new Location2D(location));
			}
		});
	}

	private final void checkIndividual(final Individual individual) {
		Preconditions.checkNotNull(individual);
		Preconditions.checkArgument(prototypeMap.containsKey(individual.getPopulation()));
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
	 * @param individual
	 * @return a deepClone of the prototype for given population registered by the underlying scenario of this simulation
	 * @throws Exception if the clone could not be created
	 */
	public Individual createClone(final Population population) {
		Preconditions.checkNotNull(population);
		Preconditions.checkState(prototypeMap.containsKey(population));
		try {
			return (Individual) objectPool.borrowObject(population);
		} catch (Exception e) {
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

				while (infinite == false
						&& runs > 0 && ! pause) {
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
		if (running == false) {
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
	 * Increase the time by one and return its (increased) value
	 * @return
	 */
	public synchronized void step() {
		space.updateTopo();
		processAgents();
		processAfterStepCommands();
		commandList.clear();
		steps.incrementAndGet();
		notifyStep();
	}

	private void processAgents() {
		FastLists.foreach(individuals, new Functor<Individual>() {
			@Override public void update(Individual individual) {
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
