package org.asoem.greyfish.core.model;

import org.asoem.greyfish.core.simulation.Simulation;

/**
 * A SimulationModel is a factory for {@link Simulation}s.
 * @param <S> the type of the simulation which will be created.
 */
public interface SimulationModel<S extends Simulation<?>> {
    /**
     * Create a new {@code Simulation}.
     * @return a new simulation
     */
    S createSimulation();
}
