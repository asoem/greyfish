/**
 * 
 */
package org.asoem.greyfish.core.conditions;

import java.util.Map;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

/**
 * This class can be used to concatenate two or more <code>Condition</code> implementations with a logical OR operator.
 * @author christoph
 *
 */
public class OrCondition extends LogicalOperatorCondition {

	/**
	 * 
	 */
	private static final long serialVersionUID = -622273184855770462L;

	public OrCondition() {
	}

	public OrCondition(OrCondition orCondition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(orCondition, mapDict);
	}

	/* (non-Javadoc)
	 * @see org.asoem.greyfish.actions.conditions.Condition#evaluate(org.asoem.greyfish.competitors.Individual)
	 */
	@Override
	public boolean evaluate(Simulation simulation) {
		boolean ret = false;
		for (GFCondition condition : this.conditions) {
			ret = ret || condition.evaluate(null);
		}
		return ret;
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new OrCondition(this, mapDict);
	}
}
