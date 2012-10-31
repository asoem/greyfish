package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.utils.base.DeepCloner;

public class AlwaysTrueCondition extends LeafCondition {

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public AlwaysTrueCondition() {
        this(new Builder());
    }

    private AlwaysTrueCondition(AlwaysTrueCondition condition, DeepCloner map) {
        super(condition, map);
    }

    private AlwaysTrueCondition(AbstractBuilder<?,?> builder) {
        super(builder);
    }

    @Override
    public boolean evaluate() {
        return true;
    }

    @Override
    public AlwaysTrueCondition deepClone(DeepCloner cloner) {
        return new AlwaysTrueCondition(this, cloner);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<AlwaysTrueCondition, Builder> {
        private Builder() {}

        @Override protected Builder self() { return this; }
        @Override public AlwaysTrueCondition checkedBuild() { return new AlwaysTrueCondition(this); }
    }

    protected static abstract class AbstractBuilder<E extends AlwaysTrueCondition, T extends AbstractBuilder<E,T>> extends LeafCondition.AbstractBuilder<E,T> {}
}
