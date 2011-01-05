package org.asoem.sico.core.conditions;

import java.util.List;
import java.util.Map;

import org.asoem.sico.utils.AbstractDeepCloneable;

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
