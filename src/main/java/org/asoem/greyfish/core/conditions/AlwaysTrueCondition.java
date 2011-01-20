package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

import java.util.Map;

public final class AlwaysTrueCondition extends LeafCondition {

    @Override
    public boolean evaluate(Simulation simulation) {
        return true;
    }

    @Override
    protected AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
    }

    private AlwaysTrueCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() { return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends IntCompareCondition.AbstractBuilder<T> {

        protected T fromClone(ActionExecutionCountCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict);
            return self();
        }

        public AlwaysTrueCondition build() { return new AlwaysTrueCondition(this); }
    }
}
