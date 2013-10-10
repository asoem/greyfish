package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;

/**
 * Default implementation of {@code SimulationContextFactory}.
 */
public final class DefaultSimulationContextFactory<S extends DiscreteTimeSimulation<A>, A extends Agent<A, S>>
        implements SimulationContextFactory<S, A> {

    private DefaultSimulationContextFactory() {
    }

    public static <S extends DiscreteTimeSimulation<A>, A extends Agent<A, S>> DefaultSimulationContextFactory<S, A> create() {
        return new DefaultSimulationContextFactory<S, A>();
    }

    @Override
    public SimulationContext<S, A> createActiveContext(final S simulation, final int agentId, final long simulationStep) {
        return DefaultActiveSimulationContext.create(simulation, agentId, simulationStep);
    }

}
