package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.CloneFactory;

import java.util.Set;

public interface SimulationFactory<S extends DiscreteTimeSimulation<A>, A extends Agent<A, ?>> {
    public Simulation<A> createSimulation(S space, Set<? extends A> prototypes, CloneFactory<A> cloneFactory);
}
