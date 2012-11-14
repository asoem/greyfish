package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.CloneFactory;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.space.Object2D;
import org.asoem.greyfish.utils.space.SpatialObject;

import java.util.Set;

/**
 * User: christoph
 * Date: 05.07.12
 * Time: 15:55
 */
public interface SimulationFactory {
    public Simulation<SpatialObject> createSimulation(Space2D<Agent, Object2D> space, Set<? extends Agent> prototypes, CloneFactory<Agent> cloneFactory);
}
