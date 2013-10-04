package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.DeepCloner;

public abstract class LongCompareCondition<A extends Agent<A, ?>> extends CompareCondition<Long, A> {
    protected LongCompareCondition() {
    }

    protected LongCompareCondition(final LongCompareCondition<A> condition, final DeepCloner map) {
        super(condition, map);
    }

    protected LongCompareCondition(final AbstractBuilder<?, ?, A> builder) {
        super(builder);
    }

    protected static abstract class AbstractBuilder<E extends LongCompareCondition<A>, T extends AbstractBuilder<E, T, A>, A extends Agent<A, ?>> extends CompareCondition.AbstractBuilder<A, E, T, Long> {
    }
}
