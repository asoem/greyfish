package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.actions.utils.ActionState;

import javax.annotation.Nullable;

/**
 * An {@code ActionExecutionStrategy} defines how instances of {@code AgentAction} get executed by the {@code Agent}
 * and logs the execution history.
 */
public interface ActionExecutionStrategy {
    /**
     * Execute the next action.
     * @return {@code true} if an action got executed, {@code false} otherwise.
     */
    boolean execute();

    /**
     * Get the last {@code AgentAction} which was executed
     * @return the last action that was executed
     */
    @Nullable
    AgentAction<?> lastExecutedAction();

    /**
     * Get the last action state of the action that was executed
     * @return the {@code ActionState} of the last executed action
     */
    @Nullable
    ActionState lastExecutedActionState();

    /**
     * Reset the history.
     */
    void reset();
}
