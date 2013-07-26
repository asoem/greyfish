package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.DeepCloner;

public class AlwaysTrueCondition<A extends Agent<A, ?>> extends LeafCondition<A> {

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public AlwaysTrueCondition() {
        this(new Builder<A>());
    }

    private AlwaysTrueCondition(final AlwaysTrueCondition<A> condition, final DeepCloner map) {
        super(condition, map);
    }

    private AlwaysTrueCondition(final AbstractBuilder<A, ?,?> builder) {
        super(builder);
    }

    @Override
    public boolean evaluate() {
        return true;
    }

    @Override
    public AlwaysTrueCondition<A> deepClone(final DeepCloner cloner) {
        return new AlwaysTrueCondition<A>(this, cloner);
    }

    public static <A extends Agent<A, ?>> Builder<A> builder() { return new Builder<A>(); }

    public static final class Builder<A extends Agent<A, ?>> extends AbstractBuilder<A, AlwaysTrueCondition<A>, Builder<A>> {
        private Builder() {}

        @Override protected Builder<A> self() { return this; }
        @Override public AlwaysTrueCondition<A> checkedBuild() { return new AlwaysTrueCondition<A>(this); }
    }

    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, E extends AlwaysTrueCondition<A>, T extends AbstractBuilder<A, E,T>> extends LeafCondition.AbstractBuilder<A, E, T> {}
}
