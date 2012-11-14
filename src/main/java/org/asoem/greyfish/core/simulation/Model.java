package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.space.Object2D;
import org.asoem.greyfish.utils.space.SpatialObject;

/**
 * User: christoph
 * Date: 08.10.12
 * Time: 14:56
 */
public interface Model<A extends Agent, S extends Space2D<A, Object2D>> {
    Simulation<SpatialObject> createSimulation(SimulationFactory simulationFactory);
}
