package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloner;

public abstract class FiniteStateAction extends AbstractGFAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(FiniteStateAction.class);
    private int statefulExecutionCount;

    protected FiniteStateAction(AbstractGFAction.AbstractBuilder<?,?> builder) {
        super(builder);
    }

    protected FiniteStateAction(FiniteStateAction cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
    }

    private Object currentStateKey = initialState();
    private boolean endStateReached = false;

    protected static class StateClass {
        private StateClass() {
        }
    }
    private static final StateClass INITIAL = new StateClass();
    private static final StateClass INTERMEDIATE = new StateClass();
    private static final StateClass END = new StateClass();

    @Override
    protected final ActionState executeUnconditioned(Simulation simulation) {

        if (endStateReached)
            resetTransition(initialState());

        Object nextStateKey = executeState(currentStateKey, simulation);
        if (nextStateKey == null)
            throw new NullPointerException("An implementation of executeState() must not return null");

        ++statefulExecutionCount;

        if (endStateReached)
            return ActionState.END_SUCCESS;
        else
            return ActionState.ACTIVE;
    }

    protected abstract Object initialState();

    protected abstract StateClass executeState(Object state, Simulation simulation);

    protected final <T> StateClass resetTransition(T state) {
        currentStateKey = state;
        LOGGER.debug("{}: Reset to {}", this, state);
        return INITIAL;
    }

    protected final <T> StateClass transition(T state) {
        currentStateKey = state;
        LOGGER.debug("{}: Transition to {}", this, state);
        return INTERMEDIATE;
    }

    protected final <T> StateClass failure(T state) {
        currentStateKey = state;
        endStateReached = true;
        LOGGER.debug("{}: Failure: {}", this, state);
        return END;
    }

    protected final <T> StateClass endTransition(T state) {
        currentStateKey = state;
        endStateReached = true;
        LOGGER.debug("{}: End transition: {}", this, state);
        return END;
    }

    protected final AssertionError unknownState() {
        LOGGER.error("{}: Unknown State: {}", this, currentStateKey);
        return new AssertionError("The implementation of executeState() of " + this + " does not handle state '" + currentStateKey + "'");
    }

    @Override
    protected void reset() {
        super.reset();
        LOGGER.debug("{}: EndTransition to {}", this, initialState());
        currentStateKey = initialState();
    }

    @Override
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
        currentStateKey = initialState();
        statefulExecutionCount = 0;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + getName() + "|" + currentStateKey + "]@" + getAgent();
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
