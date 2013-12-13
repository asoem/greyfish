package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.ComponentContext;

/**
 * An {@code ActionExecutionStrategy} defines how instances of {@code AgentAction} get executed by the {@code Agent}
 * and logs the execution history.
 */
public interface ActionExecutionStrategy<T extends Agent<T, ?>> {
    /**
     * Execute the next action.
     * @return {@code true} if an action got executed, {@code false} otherwise.
     * @param componentContext
     */
    boolean executeNext(final ComponentContext<T, ?> componentContext);

    /**
     * Reset the history.
     */
    void reset();
}
