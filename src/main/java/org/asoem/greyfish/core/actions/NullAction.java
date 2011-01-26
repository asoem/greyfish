package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;

@ClassGroup(tags="actions")
public class NullAction extends AbstractGFAction {

    private NullAction() {
        this(new Builder());
    }

    @Override
    protected void performAction(Simulation simulation) {
        /* NOP */
    }

    protected NullAction(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<NullAction> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public NullAction build() { return new NullAction(this); }
    }

    @Override
    protected AbstractGFComponent deepCloneHelper(CloneMap cloneMap) {
        return new NullAction(this, cloneMap);
    }

    public NullAction(NullAction cloneable, CloneMap map) {
        super(cloneable, map);
    }
}
