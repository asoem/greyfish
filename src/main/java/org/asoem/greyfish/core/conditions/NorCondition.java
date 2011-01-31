package org.asoem.greyfish.core.conditions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.CloneMap;

public class NorCondition extends LogicalOperatorCondition {

    public NorCondition(NorCondition condition, CloneMap map) {
        super(condition, map);
    }

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
    public NorCondition deepCloneHelper(CloneMap map) {
        return new NorCondition(this, map);
    }

    private NorCondition() {
        this(new Builder());
    }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<NorCondition> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public NorCondition build() { return new NorCondition(this); }
        public Builder none(GFCondition ... conditions) { return super.addConditions(conditions); }
    }
}
