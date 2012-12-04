package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.io.SimulationLogger;

/**
 * User: christoph
 * Date: 08.10.12
 * Time: 14:56
 */
public interface Model<S extends SpatialSimulation<?, ?>> {
    S createSimulation(SimulationLogger logger);
}
