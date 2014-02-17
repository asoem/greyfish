package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.SpatialAgent;


public interface SimulationLoggerFactory {
    <A extends Agent<?>> SimulationLogger createLogger();

    <A extends SpatialAgent<A, ?, ?, ?>> SimulationLogger createSpatialLogger();
}
