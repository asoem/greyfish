package org.asoem.sico.core.conditions;

import java.util.Map;

import org.asoem.sico.core.actions.GFAction;
import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.lang.ClassGroup;
import org.asoem.sico.utils.AbstractDeepCloneable;
import org.asoem.sico.utils.Exporter;
import org.asoem.sico.utils.ValueAdaptor;
import org.asoem.sico.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

@ClassGroup(tags="condition")
public class LastExecutionTimeCondition extends LeafCondition {

	@Element(name="action")
	private GFAction action;
	
	@Element(name="steps")
	private int steps;
	
	public LastExecutionTimeCondition() {
	}
	protected LastExecutionTimeCondition(LastExecutionTimeCondition condition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(condition, mapDict);
		action = deepClone(condition.action, mapDict);
		steps = condition.steps;
	}

	@Override
	public boolean evaluate(Simulation simulation) {
		return action != null
			&& action.wasNotExecutedForAtLeast(simulation, steps);
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new LastExecutionTimeCondition(this, mapDict);
	}

	@Override
	public void export(Exporter e) {
		super.export(e);
		
		e.addField(new ValueAdaptor<Integer>("Steps", Integer.class, steps) {

			@Override
			protected void writeThrough(Integer arg0) {
				steps = arg0;
			}
		});
		
		e.addField(new ValueSelectionAdaptor<GFAction>("Action", GFAction.class, action, getComponentOwner().getActions()) {

			@Override
			protected void writeThrough(GFAction arg0) {
				action = arg0;
			}
		});
	}
}
