package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.Simulation;

public interface SimulationContext<S extends Simulation<A>, A extends Agent<?>> {
    /**
     * Get the simulation for this context.
     *
     * @return the simulation
     */
    S getSimulation();

    Iterable<A> getActiveAgents();

}
