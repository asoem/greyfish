package org.asoem.greyfish.core.actions;

import java.util.Map;

import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

@ClassGroup(tags="action")
public class ClonalReproductionAction extends AbstractGFAction {

	private int parameterClones = 1;

	/**
	 * 
	 */
	private static final long serialVersionUID = 8819196846768101838L;

	public ClonalReproductionAction() {
	}

	public ClonalReproductionAction(String name) {
		super(name);
	}

	protected ClonalReproductionAction(
			ClonalReproductionAction action,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(action, mapDict);
		setParameterClones(action.getParameterClones());
	}

	public int getParameterClones() {
		return parameterClones;
	}

	public void setParameterClones(int parameterClones) {
		this.parameterClones = parameterClones;
	}

	@Override
	protected void performAction(Simulation simulation) {
		for (int i = 0; i < parameterClones; i++) {
			cloneIndividual(simulation);
		}
	}

	private void cloneIndividual(Simulation simulation) {
		try {
			Individual offspring = componentOwner.createClone(simulation);
			offspring.mutate();
			simulation.addIndividual(offspring, componentOwner);
		} catch (Exception e) {
			GreyfishLogger.error("Error creating a clone", e);
		}	
	}


	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new ClonalReproductionAction(this, mapDict);
	}


}
