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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public abstract class FiniteStateAction<A extends Agent<?>> extends BaseAgentAction<A, AgentContext<A>> {

    private static final Logger logger = LoggerFactory.getLogger(FiniteStateAction.class);

    private int statefulExecutionCount = 0;
    private Object nextStateKey = initialState();
    private boolean endStateReached = false;

    protected FiniteStateAction(final AbstractBuilder<A, ? extends FiniteStateAction<A>, ? extends AbstractBuilder<A, ?, ?>> builder) {
        super(builder);
        this.statefulExecutionCount = builder.statefulExecutionCount;
        this.nextStateKey = builder.nextStateKey;
        this.endStateReached = builder.endStateReached;
    }

    @Override
    protected final ActionState proceed(final AgentContext<A> context) {

        if (endStateReached) {
            resetTransition();
        }

        executeState(nextStateKey, context);

        ++statefulExecutionCount;

        if (endStateReached) {
            return ActionState.COMPLETED;
        } else {
            return ActionState.INTERMEDIATE;
        }
    }

    protected abstract Object initialState();

    protected abstract void executeState(Object state, final AgentContext<A> context);

    protected final void resetTransition() {
        logger.debug("{}: Reset state to {}", this, initialState());
        nextStateKey = initialState();
        endStateReached = false;
    }

    protected final <T> void transition(final T state) {
        logger.debug("{}: Transition to {}", this, state);
        nextStateKey = state;
    }

    protected final void failure(final String message) {
        endStateReached = true;
        logger.debug("{}: End Transition to ERROR state: {}", this, message);
    }

    protected final <T> void endTransition(final T state) {
        logger.debug("{}: End transition to {}", this, state);
        nextStateKey = state;
        endStateReached = true;
    }

    protected final AssertionError unknownState() {
        logger.error("{}: Unknown State: {}", this, nextStateKey);
        return new AssertionError("The implementation of executeState() of " + this + " does not handle state '" + nextStateKey + "'");
    }

    @Override
    public void initialize() {
        super.initialize();
        nextStateKey = initialState();
        statefulExecutionCount = 0;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
                "[" + getName() + "@" + nextStateKey + "]";
    }

    public int getStatefulExecutionCount() {
        return statefulExecutionCount;
    }

    protected static abstract class AbstractBuilder<A extends Agent<?>, C extends FiniteStateAction<A>, B extends AbstractBuilder<A, C, B>> extends BaseAgentAction.AbstractBuilder<A, C, B, AgentContext<A>> implements Serializable {
        private int statefulExecutionCount;
        private Object nextStateKey;
        private boolean endStateReached;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(final FiniteStateAction<A> action) {
            super(action);
            this.statefulExecutionCount = action.statefulExecutionCount;
            this.nextStateKey = action.nextStateKey;
            this.endStateReached = action.endStateReached;
        }
    }
}
