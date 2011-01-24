package org.asoem.greyfish.core.conditions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

import java.util.Map;

public class NorCondition extends LogicalOperatorCondition {

    @Override
    public boolean evaluate(final Simulation simulation) {
        return ! Iterables.any(conditions, new Predicate<GFCondition>() {
            @Override
            public boolean apply(GFCondition gfCondition) {
                return gfCondition.evaluate(simulation);
            }
        });
    }

    protected NorCondition(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static Builder trueIf() { return new Builder(); }

    @Override
    protected AbstractDeepCloneable deepCloneHelper(Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return trueIf().fromClone(this, mapDict).build();
    }

    private NorCondition() {
        this(new Builder());
    }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<NorCondition> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public NorCondition build() { return new NorCondition(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LogicalOperatorCondition.AbstractBuilder<T> {
        protected T fromClone(NorCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict);
            return self();
        }
    }
}
