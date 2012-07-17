package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;

@ClassGroup(tags = "actions")
public class Suicide extends AbstractGFAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(Suicide.class);

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public Suicide() {
        this(new Builder());
    }

    @Override
    protected ActionState proceed(Simulation simulation) {
        simulation.removeAgent(agent());
        LOGGER.info("{}: Dying", agent());
        agent().logEvent(this, "dies", "");
        return ActionState.COMPLETED;
    }

    @Override
    public Suicide deepClone(DeepCloner cloner) {
        return new Suicide(this, cloner);
    }

    private Suicide(AbstractGFAction cloneable, DeepCloner map) {
        super(cloneable, map);
    }

    protected Suicide(AbstractActionBuilder<?, ?> builder) {
        super(builder);
    }

    public static Builder with() {
        return new Builder();
    }

    public static final class Builder extends AbstractActionBuilder<Suicide, Builder> {
        private Builder() {
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected Suicide checkedBuild() {
            return new Suicide(this);
        }
    }
}
