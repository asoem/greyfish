package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.CloneFactory;

import java.util.Set;

public interface SimulationFactory<S extends DiscreteTimeEnvironment<A>, A extends Agent<?>> {
    public Environment<A> createSimulation(S space, Set<? extends A> prototypes, CloneFactory<A> cloneFactory);
}
