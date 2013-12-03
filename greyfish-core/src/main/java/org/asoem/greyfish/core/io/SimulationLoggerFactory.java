package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.BasicSimulationContext;
import org.asoem.greyfish.core.agent.SimulationContext;
import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.core.simulation.SpatialSimulation2D;

/**
 * User: christoph Date: 18.12.12 Time: 11:53
 */
public interface SimulationLoggerFactory {
    <A extends Agent<A, SimulationContext<?>>> SimulationLogger<A> createLogger();

    <A extends SpatialAgent<A, ?, BasicSimulationContext<? extends SpatialSimulation2D<A, ?>, A>>> SimulationLogger<A> createSpatialLogger();
}
