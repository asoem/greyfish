package org.asoem.greyfish.core.model;

import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;

/**
 * A SimulationModel is a factory for {@link org.asoem.greyfish.core.simulation.DiscreteTimeSimulation}s.
 * @param <S> the type of the getSimulation which will be created.
 */
public interface SimulationModel<S extends DiscreteTimeSimulation<?>> {
    /**
     * Create a new {@code Simulation}.
     * @return a new getSimulation
     */
    S createSimulation();
}
