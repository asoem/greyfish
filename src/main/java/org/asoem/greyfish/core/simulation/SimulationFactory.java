package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.space.Space2D;

import java.util.Set;

/**
 * User: christoph
 * Date: 05.07.12
 * Time: 15:55
 */
public interface SimulationFactory<T extends Simulation, A extends Agent, S extends Space2D<A>> {
    public T createSimulation(S space, Set<? extends A> prototypes);
}
