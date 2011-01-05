package org.asoem.sico.core.conditions;

import java.util.Map;

import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.utils.AbstractDeepCloneable;

public class NorCondition extends OrCondition {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1676320109887998336L;

	public NorCondition() {
	}

	public NorCondition(NorCondition norCondition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(norCondition, mapDict);
	}

	@Override
	public boolean evaluate(Simulation simulation) {
		return ! super.evaluate(simulation);
	}
	
	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new NorCondition(this, mapDict);
	}
}
