package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentContext;

/**
 * An {@code ActionExecutionStrategy} defines how instances of {@code AgentAction} get executed by the {@code Agent} and
 * logs the execution history.
 */
public interface ActionExecutionStrategy<T extends Agent<T, ?>> {
    /**
     * Execute the next action.
     *
     * @param agentContext the context for the actions
     * @return {@code true} if an action got executed, {@code false} otherwise.
     */
    boolean executeNext(final AgentContext<T> agentContext);

    /**
     * Reset the history.
     */
    void reset();
}
