package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.CompareOperator;
import org.asoem.greyfish.utils.base.DeepCloner;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class CompareCondition<T extends Comparable<T>, A extends Agent<A, ?>> extends LeafCondition<A> {

    protected CompareOperator compareOperator = CompareOperator.EQUAL;

    protected T value;

    protected CompareCondition() {}

    protected CompareCondition(final CompareCondition<T, A> condition, final DeepCloner map) {
        super(condition, map);
        this.compareOperator = condition.compareOperator;
        this.value = condition.value;
    }

    protected CompareCondition(final AbstractBuilder<A, ?, ?, T> builder) {
        super(builder);
        this.compareOperator = builder.compareOperator;
        this.value = builder.value;
    }

    @Override
    public boolean evaluate() {
        return compareOperator.apply(getCompareValue(), value);
    }

    protected abstract T getCompareValue();

    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, C extends CompareCondition<?, A>, T extends AbstractBuilder<A, C, T, E>, E extends Comparable<E>> extends LeafCondition.AbstractBuilder<A, C, T> {
        private CompareOperator compareOperator;
        private E value;

        public T is(final CompareOperator compareOperator) { this.compareOperator = checkNotNull(compareOperator); return self(); }
        public T to(final E value) { this.value = checkNotNull(value); return self(); }
    }
}
