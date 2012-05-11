package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;

@ClassGroup(tags="actions")
public class DeathAction extends AbstractGFAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeathAction.class);

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public DeathAction() {
        this(new Builder());
	}

	@Override
	protected ActionState proceed(Simulation simulation) {
		simulation.removeAgent(agent());
        LOGGER.info("{}: Dying", agent());
        agent().logEvent(this, "dies", "");
        return ActionState.SUCCESS;
	}

    @Override
    public DeathAction deepClone(DeepCloner cloner) {
        return new DeathAction(this, cloner);
    }

    private DeathAction(AbstractGFAction cloneable, DeepCloner map) {
        super(cloneable, map);
    }

    protected DeathAction(AbstractBuilder<?,?> builder) {
        super(builder);
    }

    public static Builder with() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<DeathAction, Builder> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override protected DeathAction checkedBuild() { return new DeathAction(this); }
    }
}
