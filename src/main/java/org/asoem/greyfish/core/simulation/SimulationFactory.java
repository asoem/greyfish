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
    public <A extends Agent, S extends Space2D<A>> Simulation<A,S> createSimulation(S space, Set<? extends A> prototypes, CloneFactory<A> cloneFactory);
}
