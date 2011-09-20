package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.utils.DeepCloner;

import java.util.List;

public abstract class LeafCondition extends AbstractCondition {

	protected LeafCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
	}

    public LeafCondition(LeafCondition condition, DeepCloner map) {
        super(condition, map);
    }

    @Override
	public List<GFCondition> getChildConditions() {
		return null;
	}

	@Override
	public boolean isLeafCondition() {
		return true;
	}

}
