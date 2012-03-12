package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.simulation.Simulation;

/**
 * User: christoph
 * Date: 12.03.12
 * Time: 15:15
 */
public interface SimulationLoggerFactory {
    SimulationLogger getLogger(Simulation simulation);
}
