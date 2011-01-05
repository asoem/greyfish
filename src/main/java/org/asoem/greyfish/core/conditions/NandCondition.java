/**
 * 
 */
package org.asoem.sico.core.conditions;

import java.util.Map;

import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.utils.AbstractDeepCloneable;

/**
 * This class can be used to prefix one <code>Condition</code> with a logical NOT operator.
 * @author christoph
 *
 */
public class NandCondition extends AndCondition {

	private static final String name = "NAND";

	/**
	 * 
	 */
	private static final long serialVersionUID = 5294949469903839030L;

	public NandCondition(NandCondition nandCondition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(nandCondition, mapDict);
	}

	/* (non-Javadoc)
	 * @see org.asoem.sico.actions.conditions.Condition#evaluate(org.asoem.sico.competitors.Individual)
	 */
	@Override
	public boolean evaluate(Simulation simulation) {
		return ! super.evaluate(simulation);
	}

	@Override
	public AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new NandCondition(this, mapDict);
	}
}
