package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.SpatialAgent;

/**
 * User: christoph
 * Date: 18.12.12
 * Time: 11:53
 */
public interface SimulationLoggerFactory {
    <A extends Agent<A, ?>> SimulationLogger<A> createLogger();
    <A extends SpatialAgent<A, ?, ?>> SimulationLogger<A> createSpatialLogger();
}
