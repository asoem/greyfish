package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;

public abstract class FiniteStateAction extends AbstractGFAction {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(FiniteStateAction.class);

    private int statefulExecutionCount;

    protected FiniteStateAction(AbstractBuilder<?, ?> builder) {
        super(builder);
    }

    protected FiniteStateAction(FiniteStateAction cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
    }

    private Object nextStateKey = initialState();
    private boolean endStateReached = false;

    @Override
    protected final ActionState proceed(Simulation simulation) {

        if (endStateReached)
            resetTransition();

        executeState(nextStateKey, simulation);

        ++statefulExecutionCount;

        if (endStateReached)
            return ActionState.COMPLETED;
        else
            return ActionState.INTERMEDIATE;
    }

    protected abstract Object initialState();

    protected abstract void executeState(Object state, Simulation simulation);

    protected final void resetTransition() {
        LOGGER.debug("{}: Reset state to {}", this, initialState());
        nextStateKey = initialState();
        endStateReached = false;
    }

    protected final <T> void transition(T state) {
        LOGGER.debug("{}: Transition to {}", this, state);
        nextStateKey = state;
    }

    protected final void failure(String message) {
        endStateReached = true;
        LOGGER.debug("{}: End Transition to ERROR state: {}", this, message);
    }

    protected final <T> void endTransition(T state) {
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

    /**
     * Won't report the number af actual invocations of this {@code FiniteStateAction}. Use {@link #getStatefulExecutionCount}
     */
    @Override
    public int getCompletionCount() {
        return super.getCompletionCount();
    }

    public int getStatefulExecutionCount() {
        return statefulExecutionCount;
    }
}
