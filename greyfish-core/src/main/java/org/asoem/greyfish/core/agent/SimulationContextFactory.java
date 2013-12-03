package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;

/**
 * A factory for instances of {@code org.asoem.greyfish.core.agent.SimulationContext}
 */
public interface SimulationContextFactory<S extends DiscreteTimeSimulation<A>, A extends Agent<A, ? extends SimulationContext<S>>> {
    BasicSimulationContext<S, A> createActiveContext(final S simulation, final int agentId, final long simulationStep);
}
