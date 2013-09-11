package org.asoem.greyfish.core.agent;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.utils.base.DeepCloner;
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

    @SuppressWarnings("unchecked") // cloning is save
    protected AbstractAgentComponent(final AbstractAgentComponent<A> cloneable, final DeepCloner map) {
        map.addClone(cloneable, this);
        initializeObject(cloneable.name, map.getClone(cloneable.agent));
    }

    protected AbstractAgentComponent(final AbstractBuilder<A, ? extends AbstractAgentComponent<A>, ? extends AbstractBuilder<A,?,?>> builder) {
        initializeObject(builder.name, builder.agent);
    }

    protected AbstractAgentComponent(final String name) {
        initializeObject(name, null);
    }

    protected void initializeObject(final String name, final A agent) {
        this.name = name;
        this.agent = agent;
    }

    @Override
    @Nullable
    public A getAgent() {
        return agent;
    }

    /**
     * @return this components {@code Agent}
     * @throws IllegalStateException if this components {@code Agent} is {@code null}
     * @see #getAgent()
     */
    @Override
    public A agent() throws IllegalStateException {
        final A agent = getAgent();
        checkState(agent != null, "Cannot access agent of %s because none is attached", this);
        return agent;
    }

    @Override
    public void setAgent(@Nullable final A agent) {
        this.agent = agent;
    }

    public void setName(final String name) {
        Preconditions.checkNotNull(name);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + getName() + '}';
    }

    @Override
    public void freeze() {
    }

    @Override
    public boolean isFrozen() {
        final A agent = getAgent();
        return agent != null && agent.isFrozen();
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

    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, C extends AbstractAgentComponent<A>, B extends AbstractBuilder<A, C, B>> extends InheritableBuilder<C, B> implements Serializable {
        private String name;

        // for serialization only
        private A agent;

        protected AbstractBuilder(final AbstractAgentComponent<A> component) {
            this.agent = component.agent;
            this.name = component.name;
        }

        protected AbstractBuilder() {
        }

        public B name(final String name) {
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