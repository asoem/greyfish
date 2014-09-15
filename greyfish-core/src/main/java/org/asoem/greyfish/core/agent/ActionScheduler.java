package org.asoem.greyfish.core.agent;

/**
 * An {@code ActionScheduler} defines how instances of {@code AgentAction} get executed by the {@code Agent} and logs
 * the execution history.
 */
public interface ActionScheduler<C> {
    /**
     * Execute the next action.
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
