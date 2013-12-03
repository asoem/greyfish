package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.Simulation;

public interface SimulationContext<S extends Simulation<?>> {
    /**
     * Get the getSimulation for this context.
     *
     * @return the getSimulation
     */
    S getSimulation();
}
