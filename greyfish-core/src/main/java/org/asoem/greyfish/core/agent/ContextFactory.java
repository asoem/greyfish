package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.environment.DiscreteTimeEnvironment;

/**
 * A factory for instances of {@code org.asoem.greyfish.core.agent.SimulationContext}
 */
public interface ContextFactory<S extends DiscreteTimeEnvironment<A>, A extends Agent<?>> {
    BasicContext<S, A> createActiveContext(final S simulation, final int agentId, final long step);
}
