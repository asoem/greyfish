package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentComponent;

public interface AgentAction<A extends Agent<A, ?>> extends AgentComponent {

    /**
     * Apply the action on it's agent in the given simulation context
     *
     * @param executionContext the context for this call
     * @return the result of the application
     */
    ActionExecutionResult apply(final ExecutionContext<A> executionContext);
}
