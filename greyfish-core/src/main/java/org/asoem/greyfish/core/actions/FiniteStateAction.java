package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public abstract class FiniteStateAction<A extends Agent<A, ?>> extends AbstractAgentAction<A> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FiniteStateAction.class);

    private int statefulExecutionCount;
    private Object nextStateKey = initialState();
    private boolean endStateReached = false;

    protected FiniteStateAction(final AbstractBuilder<A, ? extends FiniteStateAction<A>, ? extends AbstractBuilder<A, ?, ?>> builder) {
        super(builder);
        this.statefulExecutionCount = builder.statefulExecutionCount;
        this.nextStateKey = builder.nextStateKey;
        this.endStateReached = builder.endStateReached;
    }

    protected FiniteStateAction(final FiniteStateAction<A> cloneable, final DeepCloner cloner) {
        super(cloneable, cloner);
        this.statefulExecutionCount = cloneable.statefulExecutionCount;
        this.nextStateKey = cloneable.nextStateKey;
        this.endStateReached = cloneable.endStateReached;
    }

    @Override
    protected final ActionState proceed() {

        if (endStateReached)
            resetTransition();

        executeState(nextStateKey);

        ++statefulExecutionCount;

        if (endStateReached)
            return ActionState.COMPLETED;
        else
            return ActionState.INTERMEDIATE;
    }

    protected abstract Object initialState();

    protected abstract void executeState(Object state);

    protected final void resetTransition() {
        LOGGER.debug("{}: Reset state to {}", this, initialState());
        nextStateKey = initialState();
        endStateReached = false;
    }

    protected final <T> void transition(final T state) {
        LOGGER.debug("{}: Transition to {}", this, state);
        nextStateKey = state;
    }

    protected final void failure(final String message) {
        endStateReached = true;
        LOGGER.debug("{}: End Transition to ERROR state: {}", this, message);
    }

    protected final <T> void endTransition(final T state) {
        LOGGER.debug("{}: End transition to {}", this, state);
        nextStateKey = state;
        endStateReached = true;
    }

    protected final AssertionError unknownState() {
        LOGGER.error("{}: Unknown State: {}", this, nextStateKey);
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
                "[" + getName() + "@" + nextStateKey + "]" +
                "°<" + ((getAgent() == null) ? "null" : String.valueOf(agent().getId()) + "><"
        );
    }

    public int getStatefulExecutionCount() {
        return statefulExecutionCount;
    }

    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, C extends FiniteStateAction<A>, B extends AbstractBuilder<A, C, B>> extends AbstractAgentAction.AbstractBuilder<A, C, B> implements Serializable {
        private int statefulExecutionCount;
        private Object nextStateKey;
        private boolean endStateReached;

        protected AbstractBuilder() {}

        protected AbstractBuilder(final FiniteStateAction<A> action) {
            super(action);
            this.statefulExecutionCount = action.statefulExecutionCount;
            this.nextStateKey = action.nextStateKey;
            this.endStateReached = action.endStateReached;
        }
    }
}
