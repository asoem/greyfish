package org.asoem.greyfish.core.actions;

/**
 * Defined the result of an {@link AgentAction#apply(Object) action execution}.
 */
public enum ActionExecutionResult {
    /**
     * Indicates that the action should be the last in the current execution chain. All following actions won't get executed
     * this step.
     */
    BREAK,
    /**
     * Indicates that the action should be the last in the current execution chain and the next execution chain should
     * continue this this action.
     */
    CONTINUE,
    /**
     * Indicates that the next action in the execution chain should be tried.
     */
    NEXT
}
