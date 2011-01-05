/**
 * 
 */
package org.asoem.sico.core.conditions;

import java.util.Arrays;
import java.util.Map;

import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.utils.AbstractDeepCloneable;

/**
 * This class can be used to concatenate two or more <code>Condition</code> implementations with a logical AND operator.
 * @author christoph
 *
 */
public class AndCondition extends LogicalOperatorCondition {

	public AndCondition() {
	}
	
	public AndCondition(GFCondition ... conditions) {
		addAll(Arrays.asList(conditions));
	}

	protected AndCondition(AndCondition andCondition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(andCondition, mapDict);
	}

	/* (non-Javadoc)
	 * @see org.asoem.sico.actions.conditions.Condition#evaluate(org.asoem.sico.competitors.Individual)
	 */
	@Override
	public boolean evaluate(Simulation simulation) {
		boolean ret = true;
		for (GFCondition condition : conditions) {
			ret = ret && condition.evaluate(simulation);
		}
		return ret;
	}

	@Override
	public AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new AndCondition(this, mapDict);
	}
}
