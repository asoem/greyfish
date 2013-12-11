package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentComponent;

public interface AgentAction<A extends Agent<A, ?>> extends AgentComponent<A> {

    /**
     * Apply the action on it's agent in the given simulation context
     *
     * @return the result of the application
     * @param componentContext the context for this call
     */
    ActionExecutionResult apply(final ComponentContext<A, ?> componentContext);
}
