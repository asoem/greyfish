package org.asoem.greyfish.core.individual;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.DeepCloner;
import org.simpleframework.xml.Attribute;

import javax.annotation.Nullable;

public abstract class AbstractAgentComponent implements AgentComponent {

    @Attribute(name="name", required = false)
    protected String name = "";

    protected Optional<Agent> agent = Optional.absent();

    protected AbstractAgentComponent() {}

    protected AbstractAgentComponent(AbstractAgentComponent cloneable, DeepCloner map) {
        map.setAsCloned(cloneable, this);
        this.agent = Optional.fromNullable(map.cloneField(agent.orNull(), Agent.class));
        this.name = cloneable.name;
    }

    protected AbstractAgentComponent(AbstractBuilder<? extends AbstractAgentComponent, ? extends AbstractBuilder> builder) {
        this.name = builder.name;
    }

    public static abstract class AbstractBuilder<E extends AbstractAgentComponent, T extends AbstractBuilder<E, T>> extends org.asoem.greyfish.lang.AbstractBuilder<E, T> {
        private String name = "";

        public T name(String name) { this.name = name; return self(); }
    }

    @Override
    @Nullable
    public Agent getAgent() {
        return agent.orNull();
    }

    @Override
    public void setAgent(@Nullable Agent agent) {
        this.agent = Optional.fromNullable(agent);
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
    public boolean hasName(String s) {
        return name.equals(s);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + name + ']' + "@" + agent;
    }

    @Override
    public void freeze() {}

    public final void checkNotFrozen() {
        if (isFrozen()) throw new IllegalStateException("Component is frozen");
    }

    @Override
    public boolean isFrozen() {
        return agent.isPresent() && agent.get().isFrozen();
    }

    @Override
    public void prepare(Simulation context) {
    }

    @Override
    public void configure(ConfigurationHandler e) {
        if (!agent.isPresent())
            throw new IllegalStateException();

        e.setWriteProtection(new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return isFrozen();
            }
        });
    }

    @Override
    public void accept(ComponentVisitor visitor) {
        visitor.visit(this);
    }
}
