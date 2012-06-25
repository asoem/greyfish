package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;

@ClassGroup(tags = "actions")
public class NullAction extends AbstractGFAction {

    @SuppressWarnings("UnusedDeclaration")// Needed for construction by reflection / deserialization
    public NullAction() {
        this(new Builder());
    }

    @Override
    protected ActionState proceed(Simulation simulation) {
        /* NOP */
        return ActionState.COMPLETED;
    }

    protected NullAction(AbstractActionBuilder<?, ?> builder) {
        super(builder);
    }

    public static Builder with() {
        return new Builder();
    }

    public static final class Builder extends AbstractActionBuilder<NullAction, Builder> {
        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public NullAction checkedBuild() {
            return new NullAction(this);
        }
    }

    @Override
    public AbstractAgentComponent deepClone(DeepCloner cloner) {
        return new NullAction(this, cloner);
    }

    public NullAction(NullAction cloneable, DeepCloner map) {
        super(cloneable, map);
    }
}
