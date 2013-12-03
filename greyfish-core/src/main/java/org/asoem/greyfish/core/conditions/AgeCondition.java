package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Tagged;

@Tagged("conditions")
public class AgeCondition<A extends Agent<A, ?>> extends LongCompareCondition<A> {

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public AgeCondition() {}

    private AgeCondition(final AgeCondition<A> condition, final DeepCloner map) {
        super(condition, map);
    }

    private AgeCondition(final AbstractBuilder<?, ?, A> builder) {
        super(builder);
    }

    @Override
    public AgeCondition<A> deepClone(final DeepCloner cloner) {
        return new AgeCondition<A>(this, cloner);
    }

    @Override
    protected Long getCompareValue() {
        return agent().get().getContext().get().getAge();
    }

    public static final class Builder<A extends Agent<A, ?>> extends AbstractBuilder<AgeCondition<A>, Builder<A>, A> {
        @Override protected Builder<A> self() { return this; }
        @Override protected AgeCondition<A> checkedBuild() { return new AgeCondition<A>(this); }
    }

    protected static abstract class AbstractBuilder<E extends AgeCondition<A>, T extends AbstractBuilder<E, T, A>, A extends Agent<A, ?>> extends LongCompareCondition.AbstractBuilder<E, T, A> {}
}
