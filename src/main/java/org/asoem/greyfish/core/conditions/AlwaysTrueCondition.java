package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.DeepCloner;

public final class AlwaysTrueCondition extends LeafCondition {

    protected AlwaysTrueCondition(AlwaysTrueCondition condition, DeepCloner map) {
        super(condition, map);
    }

    @Override
    public boolean evaluate(ParallelizedSimulation simulation) {
        return true;
    }

    @Override
    public AbstractAgentComponent deepClone(DeepCloner cloner) {
        return new AlwaysTrueCondition(this, cloner);
    }

    private AlwaysTrueCondition() {
        this(new Builder());
    }

    private AlwaysTrueCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<AlwaysTrueCondition> {
        private Builder() {}

        @Override protected Builder self() { return this; }
        @Override public AlwaysTrueCondition build() { return new AlwaysTrueCondition(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LeafCondition.AbstractBuilder<T> {}
}
