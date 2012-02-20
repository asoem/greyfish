package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;

public abstract class FiniteStateAction extends AbstractGFAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(FiniteStateAction.class);
    private int statefulExecutionCount;

    protected FiniteStateAction(AbstractGFAction.AbstractBuilder<?,?> builder) {
        super(builder);
    }

    protected FiniteStateAction(FiniteStateAction cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
    }

    private Object nextStateKey = initialState();
    private boolean endStateReached = false;

    protected static class StateClass {
        private StateClass() {
        }
    }

    @Override
    protected final ActionState executeUnconditioned(Simulation simulation) {

        if (endStateReached)
            resetTransition();

        try {
            executeState(nextStateKey, simulation);
        }
        catch (RuntimeException e) {
            endStateReached = true;
            LOGGER.error("Caught exception during execution in state {} for simulation {}", nextStateKey, simulation, e);
            return ActionState.END_FAILED;
        }

        ++statefulExecutionCount;

        if (endStateReached)
            return ActionState.END_SUCCESS;
        else
            return ActionState.ACTIVE;
    }

    protected abstract Object initialState();

    protected abstract void executeState(Object state, Simulation simulation);

    protected final void resetTransition() {
        LOGGER.debug("{}: Reset to {}", this, initialState());
        nextStateKey = initialState();
        endStateReached = false;
    }

    protected final <T> void transition(T state) {
        LOGGER.debug("{}: Transition to {}", this, state);
        nextStateKey = state;
    }

    protected final void failure(String message) {
        endStateReached = true;
        LOGGER.debug("{}: Failure: {}", this, message);
    }

    protected final <T> void endTransition(T state) {
        LOGGER.debug("{}: End transition: {}", this, state);
        nextStateKey = state;
        endStateReached = true;
    }

    protected final AssertionError unknownState() {
        LOGGER.error("{}: Unknown State: {}", this, nextStateKey);
        return new AssertionError("The implementation of executeState() of " + this + " does not handle state '" + nextStateKey + "'");
    }

    @Override
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
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
    public int getExecutionCount() {
        return super.getExecutionCount();
    }

    public int getStatefulExecutionCount() {
        return statefulExecutionCount;
    }
}
