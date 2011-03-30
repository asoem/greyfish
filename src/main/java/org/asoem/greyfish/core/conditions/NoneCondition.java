package org.asoem.greyfish.core.conditions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.CloneMap;

public class NoneCondition extends LogicalOperatorCondition {

    public NoneCondition(NoneCondition condition, CloneMap map) {
        super(condition, map);
    }

    @Override
    public boolean evaluate(final Simulation simulation) {
        switch (conditions.size()) {
            case 0 : return true;
            case 1 : return ! conditions.get(0).evaluate(simulation);
            case 2 : return ! conditions.get(0).evaluate(simulation) && ! conditions.get(1).evaluate(simulation);
            default : return ! Iterables.any(conditions, new Predicate<GFCondition>() {
                @Override
                public boolean apply(GFCondition condition) {
                    return condition.evaluate(simulation);
                }
            });
        }
    }

    protected NoneCondition(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static Builder trueIf() { return new Builder(); }

    @Override
    public NoneCondition deepCloneHelper(CloneMap map) {
        return new NoneCondition(this, map);
    }

    @SimpleXMLConstructor
    private NoneCondition() {
        this(new Builder());
    }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<NoneCondition> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public NoneCondition build() { return new NoneCondition(this); }
        public Builder none(GFCondition ... conditions) { return super.addConditions(conditions); }
    }
}
