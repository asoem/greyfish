package org.asoem.greyfish.core.conditions;

import java.util.Map;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

public class LastExecutedActionCondition extends LeafCondition {

	@Element(name="action", required=false)
	private GFAction parameterAction;
	
	public LastExecutedActionCondition() {
	}

	public LastExecutedActionCondition(
			LastExecutedActionCondition condition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(condition, mapDict);
		this.parameterAction = deepClone(condition.parameterAction, mapDict);
	}

	@Override
	public boolean evaluate(Simulation simulation) {
		return isSameAction(parameterAction, getComponentOwner().getLastExecutedAction());
	}
	
	private boolean isSameAction(GFAction a1, GFAction a2) {
		if (a1 != null
				&& a2 != null) {
			return a1.equals(a2);
		}
		return false;
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new LastExecutedActionCondition(this, mapDict);
	}
	
	@Override
	public void export(Exporter e) {
		e.addField( new ValueSelectionAdaptor<GFAction>("Action", GFAction.class, parameterAction, componentOwner.getActions()) {
			@Override
			protected void writeThrough(GFAction arg0) {
				parameterAction = arg0;
			}
		});
	}
}
