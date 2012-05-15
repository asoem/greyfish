package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.simpleframework.xml.Attribute;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkState;

public abstract class AbstractAgentComponent implements AgentComponent {

    @Attribute(name="name", required = false)
    private String name = "";

    @Nullable
    private Agent agent;

    protected AbstractAgentComponent() {}

    protected AbstractAgentComponent(AbstractAgentComponent cloneable, DeepCloner map) {
        map.addClone(this);
        this.agent = map.cloneField(agent, Agent.class);
        this.name = cloneable.name;
    }

    protected AbstractAgentComponent(AbstractComponentBuilder<? extends AbstractAgentComponent, ? extends AbstractComponentBuilder> builder) {
        this.name = builder.name;
    }

    protected AbstractAgentComponent(String name) {
        this.name = name;
    }

    @Override
    @Nullable
    public Agent getAgent() {
        return agent;
    }

    /**
     *
     * @return this components {@code Agent}
     * @throws IllegalStateException if this components {@code Agent} is {@code null}
     * @see #getAgent()
     */
    public final Agent agent() throws IllegalStateException {
        checkState(agent != null);
        return agent;
    }

    @Override
    public void setAgent(@Nullable Agent agent) {
        this.agent = agent;
        for (AgentComponent component : children())
            component.setAgent(agent);
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
        return this.getClass().getSimpleName() + "[" + name + ']';
    }

    @Override
    public void freeze() {}

    @Override
    public boolean isFrozen() {
        return agent != null && agent.isFrozen();
    }

    @Override
    public void initialize() {
    }

    @Override
    public void configure(ConfigurationHandler e) {
        checkState(agent != null);

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

    @Override
    public void accept(ComponentVisitor visitor) {
        visitor.visit(this);
    }

    public static abstract class AbstractComponentBuilder<E extends AbstractAgentComponent, T extends AbstractComponentBuilder<E, T>> extends org.asoem.greyfish.utils.base.AbstractBuilder<E, T> {
        private String name = "";

        public T name(String name) { this.name = name; return self(); }
    }
}
