package org.asoem.greyfish.impl.simulation;

import org.asoem.greyfish.core.simulation.Simulation;

/**
 * The common type for all events which happen in and are published by
 * {@link org.asoem.greyfish.core.simulation.Simulation simulations}
 */
public interface SimulationEvent {
    Simulation<?> getSimulation();
}
