package org.asoem.greyfish.core.conditions;

import java.util.List;
import java.util.Map;

import org.asoem.greyfish.utils.AbstractDeepCloneable;

public abstract class LeafCondition extends AbstractCondition {

	private static final long serialVersionUID = -2911040919987376926L;

	public LeafCondition() {
	}

	protected LeafCondition(
			LeafCondition actionExecutionCountCondition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(actionExecutionCountCondition, mapDict);
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
