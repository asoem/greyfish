package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.agent.AgentComponent;

public interface AgentAction<C> extends AgentComponent<C> {

    /**
     * Apply the action on it's agent in the given simulation context
     *
     * @param context the context for the action
     * @return the result of the application
     */
    ActionExecutionResult apply(C context);
}
