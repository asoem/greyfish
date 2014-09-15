package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.DiscreteTimeEnvironment;

/**
 * A factory for instances of {@code org.asoem.greyfish.core.agent.SimulationContext}
 */
public interface SimulationContextFactory<S extends DiscreteTimeEnvironment<A>, A extends Agent<?>> {
    BasicSimulationContext<S, A> createActiveContext(final S simulation, final int agentId, final long simulationStep);
}
