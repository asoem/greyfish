package org.asoem.greyfish.core.actions;

import java.util.Map;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

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
