package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.agent.AbstractAgentComponent;

public abstract class AbstractAgentAction<C>
        extends AbstractAgentComponent<C> implements AgentAction<C> {

    public AbstractAgentAction(final String name) {
        super(name);
    }

    @Override
    public <T> T tell(final C context, final Object message, final Class<T> replyType) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
