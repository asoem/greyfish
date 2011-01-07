package org.asoem.greyfish.core.conditions;

import java.util.ArrayList;
import java.util.Map;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.actions.NullAction;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.simpleframework.xml.Element;

public class ExitValueCondition extends IntCompareCondition {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1945674389029474126L;
	
	@Element(name="action")
	private GFAction parameterAction;

	public ExitValueCondition(ExitValueCondition condition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(condition, mapDict);
		this.parameterAction = deepClone(condition.getParameterAction(), mapDict);
	}

	public GFAction getParameterAction() {
		return parameterAction;
	}

	public void setParameterAction(GFAction parameterAction) {
		this.parameterAction = parameterAction;
	}

	public GFAction[] valuesParameterAction() {
		final ArrayList<GFAction> actions = new ArrayList<GFAction>();
		actions.add(new NullAction());
		actions.addAll(getComponentOwner().getActions());
		return actions.toArray(new GFAction[actions.size()]);
	}

	@Override
	protected Integer getCompareValue(Simulation simulation) {
		return parameterAction.getExitValue();
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new ExitValueCondition(this, mapDict);
	}
}
