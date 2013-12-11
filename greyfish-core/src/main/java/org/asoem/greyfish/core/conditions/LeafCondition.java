package org.asoem.greyfish.core.conditions;

import com.google.common.base.Objects;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;

import java.util.Collections;
import java.util.List;

public abstract class LeafCondition<A extends Agent<A, ?>> extends AbstractCondition<A> {

    protected LeafCondition() {
    }

    protected LeafCondition(final AbstractBuilder<A, ?, ?> builder) {
        super(builder);
    }

    @Override
    public final List<ActionCondition<A>> getChildConditions() {
        return Collections.emptyList();
    }

    @Override
    public final boolean isLeafCondition() {
        return true;
    }

    @Override
    public final Iterable<AgentNode> children() {
        return Collections.emptyList();
    }

    @Override
    public final void remove(final ActionCondition<A> condition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void removeAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void add(final ActionCondition<A> condition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insert(final ActionCondition<A> condition, final int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return (isRootCondition() ? "*" : "") + Objects.toStringHelper(this).toString();
    }

    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, C extends AbstractCondition<A>, B extends AbstractBuilder<A, C, B>> extends AbstractCondition.AbstractBuilder<A, C, B> {
        protected AbstractBuilder(final LeafCondition<A> leafCondition) {
            super(leafCondition);
        }

        protected AbstractBuilder() {
        }
    }
}
