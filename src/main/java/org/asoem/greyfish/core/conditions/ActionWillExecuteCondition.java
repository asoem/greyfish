package org.asoem.greyfish.core.conditions;

import java.util.Map;

import org.asoem.greyfish.core.actions.AbstractGFAction;
import org.asoem.greyfish.core.actions.NullAction;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

/**
 * @author christoph
 * Pre-Evaluate an other action
 */
public class ActionWillExecuteCondition extends LeafCondition {

	private static final long serialVersionUID = -890674817061339870L;
	
	
	private AbstractGFAction parameterAction;

	public ActionWillExecuteCondition() {
	}

	private ActionWillExecuteCondition(
			ActionWillExecuteCondition actionWillExecuteCondition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		this.parameterAction = deepClone(actionWillExecuteCondition.parameterAction, mapDict);
	}

	@Override
	public boolean evaluate(Simulation simulation) {
		return parameterAction.evaluate(simulation);
	}

	@Override
	public void initialize(Simulation simulation) {
		super.initialize(simulation);
		if (parameterAction == null) {
			parameterAction = new NullAction();
		}
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new ActionWillExecuteCondition(this, mapDict);
	}
}
