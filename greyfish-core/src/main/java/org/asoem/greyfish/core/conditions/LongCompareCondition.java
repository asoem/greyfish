package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.Agent;

public abstract class LongCompareCondition<A extends Agent<A, ?>> extends CompareCondition<Long, A> {
    protected LongCompareCondition() {
    }

    protected LongCompareCondition(final AbstractBuilder<?, ?, A> builder) {
        super(builder);
    }

    protected static abstract class AbstractBuilder<E extends LongCompareCondition<A>, T extends AbstractBuilder<E, T, A>, A extends Agent<A, ?>> extends CompareCondition.AbstractBuilder<A, E, T, Long> {
    }
}
