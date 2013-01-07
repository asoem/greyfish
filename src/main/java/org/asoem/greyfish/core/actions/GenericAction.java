package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.utils.base.Callbacks.call;

/**
 * User: christoph
 * Date: 20.02.12
 * Time: 18:24
 */
@Tagged("actions")
public class GenericAction<A extends Agent<A, ?>> extends AbstractAgentAction<A> {

    private Callback<? super GenericAction<A>, Void> callback;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public GenericAction() {
        super(new Builder());
    }

    protected GenericAction(GenericAction<A> genericAction, CloneMap cloner) {
        super(genericAction, cloner);
        this.callback = genericAction.callback;
    }

    protected GenericAction(AbstractBuilder<A, ? extends GenericAction<A>, ? extends AbstractBuilder<A,?,?>> builder) {
        super(builder);
        this.callback = builder.callback;
    }

    @Override
    protected ActionState proceed() {
        call(callback, this);
        return ActionState.COMPLETED;
    }

    @Override
    public DeepCloneable deepClone(CloneMap cloneMap) {
        return new GenericAction(this, cloneMap);
    }

    public static <A extends Agent<A, ?>> Builder<A> builder() {
        return new Builder<A>();
    }

    public Callback<? super GenericAction<A>, Void> getCallback() {
        return callback;
    }

    public static class Builder<A extends Agent<A, ?>> extends AbstractBuilder<A, GenericAction<A>, Builder<A>> {

        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected GenericAction<A> checkedBuild() {
            return new GenericAction(this);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, C extends GenericAction<A>, B extends AbstractBuilder<A, C, B>> extends AbstractAgentAction.AbstractBuilder<A, C, B> {

        public Callback<? super GenericAction<A>, Void> callback = Callbacks.emptyCallback();

        public B executes(Callback<? super GenericAction<A>, Void> callback) {
            this.callback = checkNotNull(callback);
            return self();
        }
    }
}
