package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

import java.util.Map;

@ClassGroup(tags="actions")
public class NullAction extends AbstractGFAction {

    private NullAction() {
        this(new Builder());
    }

    @Override
    protected void performAction(Simulation simulation) {
        /* NOP */
    }

    @Override
    protected AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
    }

    protected NullAction(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() {  return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFAction.AbstractBuilder<T> {

        protected T fromClone(NullAction action, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(action, mapDict);
            return self();
        }

        public NullAction build() { return new NullAction(this); }
    }
}
