package org.asoem.greyfish.core.agent;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.asoem.greyfish.utils.base.InheritableBuilder;

import javax.annotation.Nullable;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkState;

public abstract class AbstractAgentComponent<A extends Agent<A, ?>> implements AgentComponent<A> {

    private String name;

    @Nullable
    private A agent;

    protected AbstractAgentComponent() {
        initializeObject("", null);
    }

    protected AbstractAgentComponent(final AbstractBuilder<A, ? extends AbstractAgentComponent<A>, ? extends AbstractBuilder<A, ?, ?>> builder) {
        initializeObject(builder.name, builder.agent);
    }

    protected AbstractAgentComponent(final String name) {
        initializeObject(name, null);
    }

    protected void initializeObject(final String name, final A agent) {
        this.name = name;
        this.agent = agent;
    }

    /**
     * @return this components optional {@code Agent}
     */
    @Override
    public final Optional<A> agent() {
        return Optional.fromNullable(agent);
    }

    @Override
    public final void setAgent(@Nullable final A agent) {
        this.agent = agent;
    }

    public final void setName(final String name) {
        Preconditions.checkNotNull(name);
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
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AbstractAgentComponent that = (AbstractAgentComponent) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Nullable
    @Override
    public final AgentNode parent() {
        return agent().orNull();
    }

    /**
     * Get all children of this node. <p>This default implementation simple returns an empty list but other
     * implementations might overwrite this method, if they add nodes to the tree.</p>
     */
    @Override
    public Iterable<AgentNode> children() {
        return ImmutableList.of();
    }

    protected abstract static class AbstractBuilder<A extends Agent<A, ?>, C extends AbstractAgentComponent<A>, B extends AbstractBuilder<A, C, B>> extends InheritableBuilder<C, B> implements Serializable {
        private String name;

        // for serialization only
        private A agent;

        protected AbstractBuilder(final AbstractAgentComponent<A> component) {
            this.agent = component.agent;
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
