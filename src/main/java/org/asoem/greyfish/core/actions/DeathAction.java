package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;

@ClassGroup(tags="actions")
public class DeathAction extends AbstractGFAction {

    @SimpleXMLConstructor
	private DeathAction() {
        this(new Builder());
	}

	@Override
	protected void executeUnconditioned(Simulation simulation) {
		simulation.removeAgent((Agent) getComponentOwner());
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
