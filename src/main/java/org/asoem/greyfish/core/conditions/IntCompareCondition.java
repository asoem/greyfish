package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.DeepCloner;

public abstract class IntCompareCondition<A extends Agent<A, ?>> extends CompareCondition<Integer, A> {

    protected IntCompareCondition() {
    }

    protected IntCompareCondition(IntCompareCondition<A> condition, DeepCloner map) {
        super(condition, map);
    }

    protected IntCompareCondition(AbstractBuilder<?, ?, A> builder) {
        super(builder);
    }

    protected static abstract class AbstractBuilder<E extends IntCompareCondition<A>, T extends AbstractBuilder<E, T, A>, A extends Agent<A, ?>> extends CompareCondition.AbstractBuilder<A, E, T, Integer> {
    }
}
