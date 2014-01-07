package org.asoem.greyfish.core.agent;

/**
 * An {@code ActionExecutionStrategy} defines how instances of {@code AgentAction} get executed by the {@code Agent} and
 * logs the execution history.
 */
public interface ActionExecutionStrategy<C> {
    /**
     * Execute the next action.
     *
     *
     * @param context the context for the actions
     * @return {@code true} if an action got executed, {@code false} otherwise.
     */
    boolean executeNext(C context);

    /**
     * Reset the history.
     */
    void reset();
}
