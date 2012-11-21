package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.CloneFactory;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.space.Object2D;

import java.util.Set;

/**
 * User: christoph
 * Date: 05.07.12
 * Time: 15:55
 */
public interface SimulationFactory<S extends Simulation<S, A, Z, P>, A extends Agent<A, S, P>, Z extends Space2D<A, P>, P extends Object2D>  {
    public Simulation<S,A,Z,P> createSimulation(S space, Set<? extends A> prototypes, CloneFactory<A> cloneFactory);
}
