package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.CloneFactory;

import java.util.Set;

/**
 * User: christoph
 * Date: 05.07.12
 * Time: 15:55
 */
public interface SimulationFactory<S extends Simulation<A>, A extends Agent<A, S>>  {
    public Simulation<A> createSimulation(S space, Set<? extends A> prototypes, CloneFactory<A> cloneFactory);
}
