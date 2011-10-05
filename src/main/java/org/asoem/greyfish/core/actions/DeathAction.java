package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.DeepCloner;

@ClassGroup(tags="actions")
public class DeathAction extends AbstractGFAction {

    @SimpleXMLConstructor
	private DeathAction() {
        this(new Builder());
	}

	@Override
	protected State executeUnconditioned(Simulation simulation) {
		simulation.removeAgent(agent.get());
        return State.END_SUCCESS;
	}

    @Override
    public DeathAction deepClone(DeepCloner cloner) {
        return new DeathAction(this, cloner);
    }

    private DeathAction(AbstractGFAction cloneable, DeepCloner map) {
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
