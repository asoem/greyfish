package org.asoem.greyfish.core.agent;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.InheritableBuilder;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.simpleframework.xml.Attribute;

import javax.annotation.Nullable;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkState;

public abstract class AbstractAgentComponent<A extends Agent<A, ?>> implements AgentComponent<A> {

    @Attribute(name = "name", required = false)
    private String name;

    @Nullable
    private A agent;

    protected AbstractAgentComponent() {
        initializeObject("", null);
    }

    @SuppressWarnings("unchecked") // cloning is save
    protected AbstractAgentComponent(AbstractAgentComponent<A> cloneable, DeepCloner map) {
        map.addClone(cloneable, this);
        initializeObject(cloneable.name, (A) map.getClone(cloneable.agent));
    }

    protected AbstractAgentComponent(AbstractBuilder<A, ? extends AbstractAgentComponent<A>, ? extends AbstractBuilder<A,?,?>> builder) {
        initializeObject(builder.name, builder.agent);
    }

    protected AbstractAgentComponent(String name) {
        initializeObject(name, null);
    }

    protected void initializeObject(String name, A agent) {
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
        checkState(agent != null, "This component is not attached to an agent");
        return agent;
    }

    @Override
    public void setAgent(@Nullable A agent) {
        this.agent = agent;
    }

    public void setName(String name) {
        Preconditions.checkNotNull(name);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + getName() + ']';
    }

    @Override
    public void freeze() {
    }

    @Override
    public boolean isFrozen() {
        final Agent agent = getAgent();
        return agent != null && agent.isFrozen();
    }

    @Override
    public void initialize() {
    }

    @Override
    public void configure(ConfigurationHandler e) {
        checkState(getAgent() != null);
        e.setWriteProtection(new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return isFrozen();
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractAgentComponent that = (AbstractAgentComponent) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, C extends AbstractAgentComponent<A>, B extends AbstractBuilder<A, C, B>> extends InheritableBuilder<C, B> implements Serializable {
        private String name = "";

        // for serialization only
        private A agent;

        protected AbstractBuilder(AbstractAgentComponent<A> component) {
            this.agent = component.agent;
            this.name = component.name;
        }

        protected AbstractBuilder() {
        }

        public B name(String name) {
            this.name = name;
            return self();
        }
    }
}
