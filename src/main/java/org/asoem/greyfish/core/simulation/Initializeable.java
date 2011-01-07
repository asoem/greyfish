package org.asoem.greyfish.core.simulation;

public interface Initializeable {
	/**
	 * Implement this method to perform some tasks after the an individual is given birth.
	 * (i.e. fully configured, cloned and added to a scenario in simulation state.
	 * This function will be called by the owning individuals <code>birth()</code> method.
	 * @param simulation TODO
	 * @see org.asoem.greyfish.core.individual.Individual#birth()
	 */
	public void initialize(Simulation simulation);
}
