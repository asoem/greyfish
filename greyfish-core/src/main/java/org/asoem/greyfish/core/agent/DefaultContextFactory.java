package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.environment.DiscreteTimeEnvironment;

/**
 * Default implementation of {@code SimulationContextFactory}.
 */
public final class DefaultContextFactory<S extends DiscreteTimeEnvironment<A>, A extends Agent<?>>
        implements ContextFactory<S, A> {

    private DefaultContextFactory() {
    }

    public static <S extends DiscreteTimeEnvironment<A>, A extends Agent<?>> DefaultContextFactory<S, A> create() {
        return new DefaultContextFactory<>();
    }

    @Override
    public BasicContext<S, A> createActiveContext(final S simulation, final int agentId, final long simulationStep) {
        return DefaultActiveContext.create(simulation, agentId, simulationStep);
    }

}
