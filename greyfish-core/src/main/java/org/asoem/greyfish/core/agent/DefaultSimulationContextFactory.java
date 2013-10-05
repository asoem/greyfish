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
    public ActiveSimulationContext<S, A> createActiveContext(final S simulation, final int agentId, final long simulationStep) {
        return DefaultActiveSimulationContext.create(simulation, agentId, simulationStep);
    }

    @Override
    public PassiveSimulationContext<S, A> createPassiveContext() {
        return SimulationContexts.<S, A>instance();
    }
}
