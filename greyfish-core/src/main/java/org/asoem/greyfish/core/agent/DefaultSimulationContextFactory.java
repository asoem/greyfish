package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;

/**
 * Default implementation of {@code SimulationContextFactory}.
 */
public final class DefaultSimulationContextFactory<S extends DiscreteTimeSimulation<A>, A extends Agent<?>>
        implements SimulationContextFactory<S, A> {

    private DefaultSimulationContextFactory() {
    }

    public static <S extends DiscreteTimeSimulation<A>, A extends Agent<?>> DefaultSimulationContextFactory<S, A> create() {
        return new DefaultSimulationContextFactory<>();
    }

    @Override
    public BasicSimulationContext<S, A> createActiveContext(final S simulation, final int agentId, final long simulationStep) {
        return DefaultActiveSimulationContext.create(simulation, agentId, simulationStep);
    }

}
