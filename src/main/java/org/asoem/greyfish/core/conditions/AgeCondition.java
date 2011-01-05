package org.asoem.sico.core.conditions;

import java.util.Map;

import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.lang.ClassGroup;
import org.asoem.sico.utils.AbstractDeepCloneable;

@ClassGroup(tags="condition")
public class AgeCondition extends IntCompareCondition {

	public AgeCondition() {
		super();
	}

	protected AgeCondition(AgeCondition actionExecutionCountCondition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(actionExecutionCountCondition, mapDict);
	}

	@Override
	public boolean evaluate(Simulation simulation) {
		return false;
	}
	
	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new AgeCondition(this, mapDict);
	}

	@Override
	protected Integer getCompareValue(Simulation simulation) {
		return simulation.getSteps() - componentOwner.getTimeOfBirth();
	}

}
