package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.space.Space2D;

/**
 * User: christoph
 * Date: 08.10.12
 * Time: 14:56
 */
public interface Model<S extends SpatialSimulation2D<A, ? extends Space2D<A, ?>, ?>, A extends SpatialAgent<A, S, ?>> {
    S createSimulation(SimulationLogger<A> logger);
}
