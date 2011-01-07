package org.asoem.greyfish.core.actions;

import java.util.Map;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

@ClassGroup(tags="action")
public class DeathAction extends AbstractGFAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1233134952251381297L;

	public DeathAction() {
	}

	public DeathAction(String name) {
		super(name);
	}

	protected DeathAction(DeathAction action,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(action, mapDict);
	}

	@Override
	protected void performAction(Simulation simulation) {
		//individual.death();
		simulation.removeIndividual(componentOwner);
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new DeathAction(this, mapDict);
	}

}
