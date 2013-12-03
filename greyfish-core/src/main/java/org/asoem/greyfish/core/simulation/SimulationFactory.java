package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.CloneFactory;
import org.asoem.greyfish.core.agent.SimulationContext;

import java.util.Set;

/**
 * User: christoph Date: 05.07.12 Time: 15:55
 */
public interface SimulationFactory<S extends DiscreteTimeSimulation<A>, A extends Agent<A, SimulationContext<?>>> {
    public Simulation<A> createSimulation(S space, Set<? extends A> prototypes, CloneFactory<A> cloneFactory);
}
