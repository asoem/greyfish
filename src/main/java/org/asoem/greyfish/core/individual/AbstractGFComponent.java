package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.CloneMap;
import org.simpleframework.xml.Attribute;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractGFComponent extends AbstractDeepCloneable implements GFComponent {

    private IndividualInterface componentOwner;

    @Attribute(name="name", required = false)
    protected String name = "";

    private boolean frozen = false;

    protected Simulation getSimulation() {
        return simulation;
    }

    private Simulation simulation;

    protected AbstractGFComponent() { }

    protected AbstractGFComponent(AbstractGFComponent cloneable, CloneMap map) {
        super(cloneable, map);
        this.name = cloneable.name;
    }

    @Override
    public IndividualInterface getComponentOwner() {
        return componentOwner;
    }

    @Override
    public void setComponentRoot(IndividualInterface individual) {
        componentOwner = individual;
    }

    public void setName(String name) {
        Preconditions.checkNotNull(name);
        this.name = checkFrozen(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + name + ']' + "@" + componentOwner;
    }

    @Override
    public void initialize(Simulation simulation) {
        checkNotNull(simulation);
        this.simulation = simulation;
    }

    @Override
    public void freeze() {
        frozen = true;
    }

    public final <T> T checkFrozen(T value) {
        checkNotFrozen();
        return value;
    }

    public final void checkNotFrozen() {
        if (isFrozen()) throw new IllegalStateException("Component is frozen");
    }

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) throws IllegalStateException {
        if (getComponentOwner() == null)
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
        return frozen;
    }

    @Override
    public Iterator<GFComponent> iterator() {
        return Iterators.emptyIterator();
    }
}
