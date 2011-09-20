package org.asoem.greyfish.core.actions;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.DeepCloner;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public abstract class FiniteStateAction extends AbstractGFAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(FiniteStateAction.class);

    protected FiniteStateAction(AbstractGFAction.AbstractBuilder<?> builder) {
        super(builder);
    }

    protected FiniteStateAction(FiniteStateAction cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
    }

    /* Immutable after freeze */
    private ImmutableMap<Object, FSMState> states = ImmutableMap.of();

    /* Always mutable */
    private Object currentStateKey;

    @Override
    protected final State executeUnconditioned(Simulation simulation) {
        Preconditions.checkState(currentStateKey != null);
        Preconditions.checkState(states.containsKey(currentStateKey));

        StateAction stateActionToExecute = states.get(currentStateKey).getStateAction();
        Object nextStateKey = stateActionToExecute.run(simulation);

        LOGGER.debug("{}: Transition to {}", this, nextStateKey);
        currentStateKey = nextStateKey;

        return states.get(currentStateKey).getStateType();
    }

    @Override
    protected void reset() {
        super.reset();
        LOGGER.debug("{}: EndTransition to {}", this, getInitialStateKey());
        currentStateKey = getInitialStateKey();
    }

    private Object getInitialStateKey() {
        if (states.size() == 0)
            return null;

        Object firstKey = states.keySet().asList().get(0);
        if (states.get(firstKey).getStateType() != State.DORMANT)
            return null;
        else
            return firstKey;
    }

    @Override
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
        currentStateKey = getInitialStateKey();
    }

    @Override
    public void checkConsistency() {
        super.checkConsistency();
        if (!states.isEmpty()) {
            checkState(getInitialStateKey() != null, "No InitialState defined");
            checkState(Iterables.any(states.values(), new Predicate<FSMState>() {
                @Override
                public boolean apply(FSMState state) {
                    return state.getStateType() == State.END_SUCCESS;
                }
            }), "No EndState defined");
        }
        else
            LOGGER.warn("FiniteStateAction has no states defined: " + this);
    }

    protected final void registerInitialState(final Object stateKey, final StateAction action) {
        registerStateInternal(stateKey, action, State.DORMANT);
    }

    protected final void registerIntermediateState(final Object stateKey, final StateAction action) {
        registerStateInternal(stateKey, action, State.ACTIVE);
    }

    protected final void registerEndState(final Object stateKey, final StateAction action) {
        registerStateInternal(stateKey, action, State.END_SUCCESS);
    }

    protected final void registerFailureState(final Object stateKey, final StateAction action) {
        registerStateInternal(stateKey, action, State.END_FAILED);
    }

    private void registerStateInternal(final Object stateKey, final StateAction action, final State stateType) {
        checkNotNull(stateKey);
        checkNotNull(action);
        checkNotNull(stateType);
        switch (stateType) {
            case DORMANT:
                // put in front of the map
                states = ImmutableMap.<Object, FSMState>builder().put(stateKey, new FSMState(stateType, action)).putAll(states).build();
                break;
            default:
                // put at the end of the map
                states = ImmutableMap.<Object, FSMState>builder().putAll(states).put(stateKey, new FSMState(stateType, action)).build();
                break;
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + name + "|" + currentStateKey + "]@" + getAgent();
    }

    protected interface StateAction {
        public Object run(Simulation simulation);
    }

    protected static class EndStateAction implements StateAction {
        private final Object stateKey;

        public EndStateAction(Object stateKey) {
            this.stateKey = stateKey;
        }

        @Override
        final public Object run(Simulation simulation) {
            return stateKey;
        }
    }

    protected static class FSMState {
        private final State stateType;
        private final StateAction stateAction;

        public FSMState(State stateType, StateAction stateAction) {
            Preconditions.checkNotNull(stateType);
            Preconditions.checkNotNull(stateAction);
            this.stateType = stateType;
            this.stateAction = stateAction;
        }

        public State getStateType() {
            return stateType;
        }

        public StateAction getStateAction() {
            return stateAction;
        }
    }
}
