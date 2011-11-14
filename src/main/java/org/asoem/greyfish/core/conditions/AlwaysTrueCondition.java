package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloner;

public final class AlwaysTrueCondition extends LeafCondition {

    protected AlwaysTrueCondition(AlwaysTrueCondition condition, DeepCloner map) {
        super(condition, map);
    }

    @Override
    public boolean apply(Simulation simulation) {
        return true;
    }

    @Override
    public AbstractAgentComponent deepClone(DeepCloner cloner) {
        return new AlwaysTrueCondition(this, cloner);
    }

    public AlwaysTrueCondition() {
        this(new Builder());
    }

    private AlwaysTrueCondition(AbstractBuilder<?,?> builder) {
        super(builder);
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<AlwaysTrueCondition, Builder> {
        private Builder() {}

        @Override protected Builder self() { return this; }
        @Override public AlwaysTrueCondition checkedBuild() { return new AlwaysTrueCondition(this); }
    }

    protected static abstract class AbstractBuilder<E extends AlwaysTrueCondition, T extends AbstractBuilder<E,T>> extends LeafCondition.AbstractBuilder<E,T> {}
}
