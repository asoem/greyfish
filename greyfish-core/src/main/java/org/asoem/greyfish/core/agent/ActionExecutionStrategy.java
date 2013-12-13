package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.ExecutionContext;

/**
 * An {@code ActionExecutionStrategy} defines how instances of {@code AgentAction} get executed by the {@code Agent} and
 * logs the execution history.
 */
public interface ActionExecutionStrategy<T extends Agent<T, ?>> {
    /**
     * Execute the next action.
     *
     * @param executionContext the context for the actions
     * @return {@code true} if an action got executed, {@code false} otherwise.
     */
    boolean executeNext(final ExecutionContext<T> executionContext);

    /**
     * Reset the history.
     */
    void reset();
}
