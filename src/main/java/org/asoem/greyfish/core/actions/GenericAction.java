package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.utils.base.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.utils.base.Callbacks.call;

/**
 * User: christoph
 * Date: 20.02.12
 * Time: 18:24
 */
@Tagged("actions")
public class GenericAction extends AbstractAgentAction {

    private Callback<? super GenericAction, Void> callback;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public GenericAction() {
        super(new Builder());
    }

    protected GenericAction(GenericAction genericAction, DeepCloner cloner) {
        super(genericAction, cloner);
        this.callback = genericAction.callback;
    }

    protected GenericAction(AbstractBuilder<? extends GenericAction, ? extends AbstractBuilder> builder) {
        super(builder);
        this.callback = builder.callback;
    }

    @Override
    protected ActionState proceed() {
        call(callback, this);
        return ActionState.COMPLETED;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new GenericAction(this, cloner);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Callback<? super GenericAction, Void> getCallback() {
        return callback;
    }

    public static class Builder extends AbstractBuilder<GenericAction, Builder> {

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected GenericAction checkedBuild() {
            return new GenericAction(this);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<C extends GenericAction, B extends AbstractBuilder<C, B>> extends AbstractAgentAction.AbstractBuilder<C, B> {

        public Callback<? super GenericAction, Void> callback = Callbacks.emptyCallback();

        public B executes(Callback<? super GenericAction, Void> callback) {
            this.callback = checkNotNull(callback);
            return self();
        }
    }
}
