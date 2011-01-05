package org.asoem.sico.core.simulation;


public interface SimulationManagerListener {

	public void simulationAdded(Simulation simulation);
	public void simulationRemoved(Simulation simulation);
}
