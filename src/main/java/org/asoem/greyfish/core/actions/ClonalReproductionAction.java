package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.simpleframework.xml.Attribute;

import java.util.Map;

@ClassGroup(tags="actions")
public class ClonalReproductionAction extends AbstractGFAction {

    @Attribute(name = "nClones")
	private int parameterClones = 1;

	private ClonalReproductionAction() {
        this(new Builder());
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
		return new Builder().fromClone(this, mapDict).build();
	}

    protected ClonalReproductionAction(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() {  return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFAction.AbstractBuilder<T> {
        private int parameterClones;

        public T parameterClones(int parameterClones) { this.parameterClones = parameterClones; return self(); }

        protected T fromClone(ClonalReproductionAction action, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(action, mapDict).parameterClones(action.parameterClones);
            return self();
        }

        public ClonalReproductionAction build() { return new ClonalReproductionAction(this); }
    }
}
