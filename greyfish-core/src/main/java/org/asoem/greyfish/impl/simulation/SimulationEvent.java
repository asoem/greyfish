package org.asoem.greyfish.impl.simulation;

import org.asoem.greyfish.core.simulation.Environment;

/**
 * The common type for all events which happen in and are published by {@link org.asoem.greyfish.core.simulation.Environment
 * simulations}
 */
public interface SimulationEvent {
    Environment<?> getEnvironment();
}
