package org.asoem.sico.core.conditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.asoem.sico.core.actions.GFAction;
import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.utils.AbstractDeepCloneable;
import org.asoem.sico.utils.Exporter;
import org.asoem.sico.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

public class ActionExecutionCountCondition extends IntCompareCondition {

	@Element(name="action")
	private GFAction parameterAction;
	
	public ActionExecutionCountCondition() {
	}

	public ActionExecutionCountCondition(
			ActionExecutionCountCondition actionExecutionCountCondition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(actionExecutionCountCondition, mapDict);
		this.parameterAction = deepClone(actionExecutionCountCondition.parameterAction, mapDict);
	}
	
	@Override
	protected Integer getCompareValue(Simulation simulation) {
		return parameterAction.getExecutionCount();
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new ActionExecutionCountCondition(this, mapDict);
	}
	
	@Override
	public void export(Exporter e) {
		super.export(e);
		
		final List<GFAction> list = (componentOwner != null) ? getComponentOwner().getActions() : new ArrayList<GFAction>();
		e.addField( new ValueSelectionAdaptor<GFAction>("", GFAction.class, parameterAction, list) {

			@Override
			protected void writeThrough(GFAction arg0) {
				ActionExecutionCountCondition.this.parameterAction = arg0;
			}
		});
	}
}
