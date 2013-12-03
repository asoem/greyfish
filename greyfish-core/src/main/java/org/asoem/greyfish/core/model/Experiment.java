package org.asoem.greyfish.core.model;

/**
 * An experiment executes a series of {@link org.asoem.greyfish.core.simulation.Simulation simulations}
 * in order to take certain measurements.
 */
public interface Experiment extends Runnable {
    @Deprecated
    void addSimulationListener(SimulationListener listener);
}
