package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.simpleframework.xml.Attribute;

public abstract class AbstractGFComponent extends AbstractDeepCloneable<AbstractGFComponent> implements GFComponent {

    protected Individual componentOwner;

    @Attribute(name="name", required = false)
    protected String name = "";

    private boolean frozen = false;

    protected AbstractGFComponent(AbstractGFComponent clonable, CloneMap map) {
        super(clonable, map);
        this.name = clonable.name;
    }

    @Override
    public Individual getComponentOwner() {
        return componentOwner;
    }

    @Override
    public void setComponentOwner(Individual individual) {
        componentOwner = checkFrozen(individual);
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
        return getName();
    }

    @Override
    public void initialize(Simulation simulation) {
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
    public void checkIfFreezable(Iterable<? extends GFComponent> components) throws IllegalStateException {
        if (componentOwner == null)
            throw new IllegalStateException(name + " (" + this.getClass().getSimpleName() + "): Components must have an owner");
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
}
