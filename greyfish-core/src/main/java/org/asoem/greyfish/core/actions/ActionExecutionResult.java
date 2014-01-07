package org.asoem.greyfish.core.actions;

/**
 * Defined the result of an {@link AgentAction#apply(Object) action execution}.
 */
public enum ActionExecutionResult {
    /**
     * Return if the action should be the last in the current execution chain. All following actions won't get executed
     * this step.
     */
    BREAK,
    /**
     * Return if the action should be the last in the current execution chain and the next execution chain should
     * continue this this action.
     */
    CONTINUE,
    /**
     * Try the next action in the execution chain.
     */
    NEXT
}
