package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

import java.util.Map;

@ClassGroup(tags="actions")
public class ClonalReproductionAction extends AbstractGFAction {

	private int parameterClones = 1;

	public ClonalReproductionAction() {
	}

    public ClonalReproductionAction(Builder builder) {
        this.parameterClones = builder.nClones;
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
		return new Builder().deepClone(this, mapDict).build();
	}

    public static class Builder extends AbstractGFAction.Builder {
        int nClones;

        protected Builder deepClone(ClonalReproductionAction action,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.deepClone(action, mapDict);
            nClones = action.parameterClones;
            return this;
        }

        public Builder nClones(int nClones) {
            this.nClones = nClones;
            return this;
        }

        public ClonalReproductionAction build() {
            return new ClonalReproductionAction(this);
        }
    }
}
