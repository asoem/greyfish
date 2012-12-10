package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.utils.base.DeepCloner;

import java.util.Collections;
import java.util.List;

public abstract class LeafCondition<A extends Agent<A, ?>> extends AbstractCondition<A> {

    protected LeafCondition() {}

    protected LeafCondition(LeafCondition<A> cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
    }

    protected LeafCondition(AbstractBuilder<A, ? extends LeafCondition, ? extends AbstractBuilder> builder) {
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
    public final Iterable<AgentNode> childConditions() {
        return Collections.emptyList();
    }

    @Override
    public final void remove(ActionCondition<A> condition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void removeAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void add(ActionCondition<A> condition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insert(ActionCondition<A> condition, int index) {
        throw new UnsupportedOperationException();
    }

    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, C extends AbstractCondition<A>, B extends AbstractBuilder<A, C, B>> extends AbstractCondition.AbstractBuilder<A, C, B> {
        protected AbstractBuilder(LeafCondition leafCondition) {
            super(leafCondition);
        }

        protected AbstractBuilder() {
        }
    }
}
