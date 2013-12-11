package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.BasicSimulationContext;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.utils.base.Callbacks.call;

/**
 * A generic action which uses a {@link Callback}.
 *
 * @param <A> the type of the agent to which this action will be added to
 */
public final class GenericAction<A extends Agent<A, ? extends BasicSimulationContext<?, A>>> extends BaseAgentAction<A> {

    private Callback<? super GenericAction<A>, Void> callback;

    private GenericAction(final AbstractBuilder<A, ? extends GenericAction<A>, ? extends AbstractBuilder<A, ?, ?>> builder) {
        super(builder);
        this.callback = builder.callback;
    }

    @Override
    protected ActionState proceed() {
        call(callback, this);
        return ActionState.COMPLETED;
    }

    public static <A extends Agent<A, ? extends BasicSimulationContext<?, A>>> Builder<A> builder() {
        return new Builder<A>();
    }

    public Callback<? super GenericAction<A>, Void> getCallback() {
        return callback;
    }

    public static final class Builder<A extends Agent<A, ? extends BasicSimulationContext<?, A>>> extends AbstractBuilder<A, GenericAction<A>, Builder<A>> {

        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected GenericAction<A> checkedBuild() {
            return new GenericAction<>(this);
        }
    }

    protected abstract static class AbstractBuilder<A extends Agent<A, ? extends BasicSimulationContext<?, A>>, C extends GenericAction<A>, B extends AbstractBuilder<A, C, B>> extends BaseAgentAction.AbstractBuilder<A, C, B> {

        private Callback<? super GenericAction<A>, Void> callback = Callbacks.emptyCallback();

        public B executes(final Callback<? super GenericAction<A>, Void> callback) {
            this.callback = checkNotNull(callback);
            return self();
        }
    }
}
