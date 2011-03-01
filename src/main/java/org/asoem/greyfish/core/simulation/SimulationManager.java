package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.ListenerSupport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;

import static org.asoem.greyfish.core.io.GreyfishLogger.CORE_LOGGER;


public class SimulationManager implements Iterable<Simulation> {

	private final ArrayList<Simulation> simulations = new ArrayList<Simulation>();

	private final ThreadGroup simulationThreads = new ThreadGroup("SimulationThreads") {
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			CORE_LOGGER.debug("Exception in simulation thread.", e);
		}
    };

	private final ListenerSupport<SimulationManagerListener> listenerSupport = new ListenerSupport<SimulationManagerListener>();

	public SimulationManager() {
	}

	public void addSimulation(final Simulation simulation) {
		simulations.add(simulation);
		Executors.newSingleThreadExecutor().execute(new Thread(simulationThreads, simulation));
		fireSimulationAdded(simulation);
	}

	public void addSimulationManagerListener(SimulationManagerListener listener) {
		listenerSupport.addListener(listener);
	}

	public void removeSimulationManagerListener(SimulationManagerListener listener) {
		listenerSupport.removeListener(listener);
	}

	public void stopAllSimulations() {
		for (Simulation sim : simulations) {
			sim.stop();
		}
	}

	public void resetAllSimulations() {
		for (Simulation sim : simulations) {
			sim.reset();
		}
	}

	public Simulation[] getSimulations(Scenario scenario) {
		ArrayList<Simulation> ret = new ArrayList<Simulation>();
		for (Simulation simulation : simulations) {
			if (simulation.getScenario().equals(scenario))
				ret.add(simulation);
		}
		return ret.toArray(new Simulation[ret.size()]);
	}

	public boolean remove(Simulation simulation) {
		if ( simulations.remove(simulation) ) {
			fireSimulationRemoved(simulation);
			return true;
		}
		else return false;
	}

	private void fireSimulationAdded(final Simulation simulation) {
		listenerSupport.notifyListeners(new Functor<SimulationManagerListener>() {

			@Override
			public void update(SimulationManagerListener listener) {
				listener.simulationAdded(simulation);
			}
		});
	}

	private void fireSimulationRemoved(final Simulation simulation) {
		listenerSupport.notifyListeners(new Functor<SimulationManagerListener>() {

			@Override
			public void update(SimulationManagerListener listener) {
				listener.simulationRemoved(simulation);
			}
		});
	}

	@Override
	public Iterator<Simulation> iterator() {
		return simulations.iterator();
	}
}
