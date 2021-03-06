/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.utils.base.Callbacks.call;

/**
 * A generic action which uses a {@link Callback}.
 *
 * @param <A> the type of the agent to which this action will be added to
 */
public final class GenericAction<A extends Agent<?>> extends BaseAgentAction<A, AgentContext<A>> {

    private Callback<? super GenericAction<A>, Void> callback;

    private GenericAction(final AbstractBuilder<A, ? extends GenericAction<A>, ? extends AbstractBuilder<A, ?, ?>> builder) {
        super(builder);
        this.callback = builder.callback;
    }

    @Override
    protected ActionState proceed(final AgentContext<A> context) {
        call(callback, this);
        return ActionState.COMPLETED;
    }

    public static <A extends Agent<?>> Builder<A> builder() {
        return new Builder<A>();
    }

    public Callback<? super GenericAction<A>, Void> getCallback() {
        return callback;
    }

    public static final class Builder<A extends Agent<?>> extends AbstractBuilder<A, GenericAction<A>, Builder<A>> {

        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected GenericAction<A> checkedBuild() {
            return new GenericAction<>(this);
        }
    }

    protected abstract static class AbstractBuilder<A extends Agent<?>, C extends GenericAction<A>, B extends AbstractBuilder<A, C, B>> extends BaseAgentAction.AbstractBuilder<A, C, B, AgentContext<A>> {

        private Callback<? super GenericAction<A>, Void> callback = Callbacks.emptyCallback();

        public B executes(final Callback<? super GenericAction<A>, Void> callback) {
            this.callback = checkNotNull(callback);
            return self();
        }
    }
}
