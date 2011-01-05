package org.asoem.sico.core.actions;

import java.util.Map;

import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.lang.ClassGroup;
import org.asoem.sico.utils.AbstractDeepCloneable;

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
