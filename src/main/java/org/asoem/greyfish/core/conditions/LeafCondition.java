package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.AgentComponent;
import org.asoem.greyfish.utils.base.DeepCloner;

import java.util.Collections;
import java.util.List;

public abstract class LeafCondition extends AbstractCondition {

    protected LeafCondition() {}

    protected LeafCondition(AbstractBuilder<?,?> builder) {
        super(builder);
	}

    public LeafCondition(LeafCondition condition, DeepCloner map) {
        super(condition, map);
    }

    @Override
	public final List<GFCondition> getChildConditions() {
		return Collections.emptyList();
	}

	@Override
	public final boolean isLeafCondition() {
		return true;
	}

    @Override
    public final Iterable<AgentComponent> children() {
        return Collections.emptyList();
    }

    @Override
    public final void remove(GFCondition condition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void removeAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void add(GFCondition condition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insert(GFCondition condition, int index) {
        throw new UnsupportedOperationException();
    }
}
