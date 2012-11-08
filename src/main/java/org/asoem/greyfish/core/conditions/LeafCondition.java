package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.utils.base.DeepCloner;

import java.util.Collections;
import java.util.List;

public abstract class LeafCondition extends AbstractCondition {

    protected LeafCondition() {}

    protected LeafCondition(LeafCondition cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
    }

    protected LeafCondition(AbstractBuilder<? extends LeafCondition, ? extends AbstractBuilder> builder) {
        super(builder);
    }

    @Override
	public final List<ActionCondition> getChildConditions() {
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
    public final void remove(ActionCondition condition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void removeAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void add(ActionCondition condition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insert(ActionCondition condition, int index) {
        throw new UnsupportedOperationException();
    }

    protected static abstract class AbstractBuilder<C extends AbstractCondition, B extends AbstractBuilder<C, B>> extends AbstractCondition.AbstractBuilder<C, B> {
        protected AbstractBuilder(LeafCondition leafCondition) {
            super(leafCondition);
        }

        protected AbstractBuilder() {
        }
    }
}
