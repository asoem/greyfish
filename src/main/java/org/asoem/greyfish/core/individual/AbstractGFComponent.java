package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterators;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.simpleframework.xml.Attribute;

import java.util.Iterator;

public abstract class AbstractGFComponent extends AbstractDeepCloneable implements GFComponent {

    @Attribute(name="name", required = false)
    protected String name = "";
    protected Agent agent;

    protected AbstractGFComponent() {
    }

    protected AbstractGFComponent(AbstractGFComponent cloneable, CloneMap map) {
        super(cloneable, map);
        this.name = cloneable.name;
    }

    @Override
    public Agent getAgent() {
        return agent;
    }

    @Override
    public void setAgent(Agent agent) {
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
    public boolean hasName(String s) {
        return name.equals(s);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + name + ']' + "@" + agent;
    }

    @Override
    public void freeze() {
    }

    public final void checkNotFrozen() {
        if (isFrozen()) throw new IllegalStateException("Component is frozen");
    }

    @Override
    public void checkConsistency() throws IllegalStateException {
        if (getAgent() == null)
            throw new IllegalStateException(
                    AbstractGFComponent.class.getSimpleName() + "[" + name + "]: Components must have an owner");
    }

    protected AbstractGFComponent(AbstractBuilder<?> builder) {
        this.name = builder.name;
    }

    public static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends org.asoem.greyfish.lang.AbstractBuilder<T> {
        private String name = "";

        public T name(String name) { this.name = name; return self(); }
    }

    @Override
    public boolean isFrozen() {
        return agent != null && agent.isFrozen();
    }

    @Override
    public Iterator<GFComponent> iterator() {
        return Iterators.emptyIterator();
    }

    @Override
    public void prepare(Simulation context) {
    }

    @Override
    public void configure(ConfigurationHandler e) {
        e.setWriteProtection(new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return isFrozen();
            }
        });
    }
}
