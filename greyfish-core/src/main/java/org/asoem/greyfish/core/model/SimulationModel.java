package org.asoem.greyfish.core.model;

import org.asoem.greyfish.core.simulation.DiscreteTimeEnvironment;

/**
 * A SimulationModel is a factory for {@link org.asoem.greyfish.core.simulation.DiscreteTimeEnvironment}s.
 *
 * @param <S> the type of the getSimulation which will be created.
 */
public interface SimulationModel<S extends DiscreteTimeEnvironment<?>> {
    /**
     * Create a new {@code Simulation}.
     *
     * @return a new getSimulation
     */
    S createSimulation();
}
