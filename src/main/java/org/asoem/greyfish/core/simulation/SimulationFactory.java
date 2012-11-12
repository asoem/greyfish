package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.CloneFactory;
import org.asoem.greyfish.core.space.Space2D;

import java.util.Set;

/**
 * User: christoph
 * Date: 05.07.12
 * Time: 15:55
 */
public interface SimulationFactory {
    public Simulation createSimulation(Space2D<Agent> space, Set<? extends Agent> prototypes, CloneFactory<Agent> cloneFactory);
}
