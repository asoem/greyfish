package org.asoem.greyfish.core.actions;

import com.google.common.collect.ImmutableList;
import org.asoem.greyfish.core.agent.AgentNode;

public abstract class AbstractAgentAction<C>
        implements AgentAction<C> {

    private final String name;

    public AbstractAgentAction(final String name) {
        this.name = name;
    }

    @Override
    public final <T> T ask(final C context, final Object message, final Class<T> replyType) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public void initialize() {
    }

    /**
     * Get all children of this node. <p>This default implementation simple returns an empty list but other
     * implementations might overwrite this method, if they add nodes to the tree.</p>
     */
    @Override
    public Iterable<AgentNode> children() {
        return ImmutableList.of();
    }
}
