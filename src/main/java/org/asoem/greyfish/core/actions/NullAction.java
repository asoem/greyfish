package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Tagged;

@Tagged("actions")
public class NullAction extends AbstractAgentAction {

    @SuppressWarnings("UnusedDeclaration")// Needed for construction by reflection / deserialization
    public NullAction() {
        this(new Builder());
    }

    @Override
    protected ActionState proceed() {
        /* NOP */
        return ActionState.COMPLETED;
    }

    protected NullAction(AbstractBuilder<?, ?> builder) {
        super(builder);
    }

    public static Builder with() {
        return new Builder();
    }

    public static final class Builder extends AbstractBuilder<NullAction, Builder> {
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
