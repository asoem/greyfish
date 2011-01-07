/**
 * 
 */
package org.asoem.greyfish.core.conditions;

import java.util.Map;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

/**
 * @author christoph
 *
 */
public class IntervalCondition extends DelayCondition {

	/**
	 * 
	 */
	private static final long serialVersionUID = 841512897120583828L;


	public IntervalCondition(IntervalCondition intervalCondition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(intervalCondition, mapDict);
	}

	/* (non-Javadoc)
	 * @see org.asoem.greyfish.actions.conditions.Condition#evaluate(org.asoem.greyfish.competitors.Individual)
	 */
	@Override
	public boolean evaluate(Simulation simulation) {
		if( super.evaluate(simulation) ) {
			super.reset();
			return true;
		}
		return false;
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new IntervalCondition(this, mapDict);
	}
	
}
