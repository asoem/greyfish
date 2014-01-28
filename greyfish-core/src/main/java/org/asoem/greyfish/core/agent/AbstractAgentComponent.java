package org.asoem.greyfish.core.agent;

import com.google.common.collect.ImmutableList;
import org.asoem.greyfish.utils.base.InheritableBuilder;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkState;

public abstract class AbstractAgentComponent<C> implements AgentComponent<C> {

    private final String name;

    protected AbstractAgentComponent() {
        this.name = "";
    }

    protected AbstractAgentComponent(final AbstractBuilder<?, ?> builder) {
        this.name = builder.name;
    }

    protected AbstractAgentComponent(final String name) {
        this.name = name;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + getName() + '}';
    }

    @Override
    public void initialize() {
    }

    @Override
    public <T> T tell(final C context, final Object message, final Class<T> replyType) {
        throw new UnsupportedOperationException("Cannot handle message " + message);
    }

    /**
     * Get all children of this node. <p>This default implementation simple returns an empty list but other
     * implementations might overwrite this method, if they add nodes to the tree.</p>
     */
    @Override
    public Iterable<AgentNode> children() {
        return ImmutableList.of();
    }

    protected abstract static class AbstractBuilder<
            C extends AbstractAgentComponent<?>,
            B extends AbstractBuilder<C, B>>
            extends InheritableBuilder<C, B>
            implements Serializable {
        protected String name;

        protected AbstractBuilder(final AbstractAgentComponent<?> component) {
            this.name = component.name;
        }

        protected AbstractBuilder() {
        }

        public final B name(final String name) {
            this.name = name;
            return self();
        }

        @Override
        protected void checkBuilder() {
            super.checkBuilder();
            checkState(name != null, "No name was defined");
        }
    }
}
