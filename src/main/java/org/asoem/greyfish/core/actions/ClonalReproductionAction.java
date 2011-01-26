package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.simpleframework.xml.Attribute;

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
			simulation.addNextStep(offspring, componentOwner);
		} catch (Exception e) {
			GreyfishLogger.error("Error creating a clone", e);
		}	
	}

    @Override
    protected ClonalReproductionAction deepCloneHelper(CloneMap map) {
        return new ClonalReproductionAction(this, map);
    }

    public ClonalReproductionAction(ClonalReproductionAction cloneable, CloneMap map) {
        super(cloneable, map);
        this.parameterClones = cloneable.parameterClones;
    }

    protected ClonalReproductionAction(AbstractBuilder<?> builder) {
        super(builder);
        this.parameterClones = builder.nClones;
    }

    public static final Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<ClonalReproductionAction> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public ClonalReproductionAction build() { return new ClonalReproductionAction(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFAction.AbstractBuilder<T> {
        private int nClones;

        public T clones(int parameterClones) { this.nClones = parameterClones; return self(); }
    }
}
