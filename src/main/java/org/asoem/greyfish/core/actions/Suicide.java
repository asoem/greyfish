package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;

@ClassGroup(tags = "actions")
public class Suicide extends AbstractAgentAction {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(Suicide.class);

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

    private Suicide(AbstractAgentAction cloneable, DeepCloner map) {
        super(cloneable, map);
    }

    protected Suicide(AbstractBuilder<? extends Suicide, ? extends AbstractBuilder> builder) {
        super(builder);
    }

    public static Builder with() {
        return new Builder();
    }

    public static final class Builder extends AbstractBuilder<Suicide, Builder> {
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
