package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.Environment;

public interface SimulationContext<S extends Environment<A>, A extends Agent<?>> {
    /**
     * Get the simulation for this context.
     *
     * @return the simulation
     */
    S getSimulation();

    Iterable<A> getActiveAgents();

    Iterable<A> getAgents(PrototypeGroup prototypeGroup);
}
