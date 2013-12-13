package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;

public abstract class AbstractAgentAction<A extends Agent<A, ?>>
        extends AbstractAgentComponent<A> implements AgentAction<A> {

    public AbstractAgentAction(final String name) {
        super(name);
    }
}
