package org.asoem.sico.core.actions;

import java.util.Map;

import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.lang.ClassGroup;
import org.asoem.sico.utils.AbstractDeepCloneable;

@ClassGroup(tags="action")
public class NullAction extends AbstractGFAction {

	public NullAction() {
		super();
	}

	protected NullAction(NullAction action,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(action, mapDict);
	}

	@Override
	protected void performAction(Simulation simulation) {
		/* NOP */
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new NullAction(this, mapDict);
	}

}
