package org.asoem.greyfish.core.conditions;

import java.util.List;

public abstract class LeafCondition extends AbstractCondition {

	protected LeafCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
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
