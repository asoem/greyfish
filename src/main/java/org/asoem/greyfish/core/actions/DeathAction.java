package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;

@ClassGroup(tags="actions")
public class DeathAction extends AbstractGFAction {

	private DeathAction() {
        this(new Builder());
	}

	@Override
	protected void performAction(Simulation simulation) {
		//individual.death();
		simulation.removeAgent(getComponentOwner());
	}

    @Override
    public boolean evaluate(Simulation simulation) {
        return super.evaluate(simulation);
    }

    @Override
    public DeathAction deepCloneHelper(CloneMap cloneMap) {
        return new DeathAction(this, cloneMap);
    }

    private DeathAction(AbstractGFAction cloneable, CloneMap map) {
        super(cloneable, map);
    }

    protected DeathAction(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<DeathAction> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public DeathAction build() { return new DeathAction(this); }
    }
}
