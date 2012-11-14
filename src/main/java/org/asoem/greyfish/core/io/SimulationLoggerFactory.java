package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.space.SpatialObject;

/**
 * User: christoph
 * Date: 12.03.12
 * Time: 15:15
 */
public interface SimulationLoggerFactory {
    SimulationLogger getLogger(Simulation<SpatialObject> simulation);
}
