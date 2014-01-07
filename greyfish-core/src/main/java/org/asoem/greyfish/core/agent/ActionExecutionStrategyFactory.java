package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentAction;

import java.util.List;

/**
 * A factory for instances of {@link ActionExecutionStrategy}.
 */
public interface ActionExecutionStrategyFactory<C> {

    /**
     * Create a strategy for given {@code actions}.
     *
     * @param actions the actions to execute
     * @return a new strategy
     */
    ActionExecutionStrategy<C> create(final List<? extends AgentAction<? super C>> actions);
}
