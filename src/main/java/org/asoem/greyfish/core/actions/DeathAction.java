package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.io.AgentEvent;
import org.asoem.greyfish.core.io.AgentEventLogger;
import org.asoem.greyfish.core.io.AgentEventLoggerFactory;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;

@ClassGroup(tags="actions")
public class DeathAction extends AbstractGFAction {

    private static final AgentEventLogger AGENT_EVENT_LOGGER = AgentEventLoggerFactory.getLogger();

    @SimpleXMLConstructor
	public DeathAction() {
        this(new Builder());
	}

	@Override
	protected ActionState executeUnconditioned(Simulation simulation) {
		simulation.removeAgent(agent());

        AGENT_EVENT_LOGGER.addEvent(new AgentEvent(simulation, simulation.getSteps(), agent(), this, "died", "", simulation.getSpace().getCoordinates(agent())));

        return ActionState.END_SUCCESS;
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
        @Override public DeathAction checkedBuild() { return new DeathAction(this); }
    }
}
