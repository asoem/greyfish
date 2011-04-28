package org.asoem.greyfish.core.actions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.CloneMap;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.in;
import static com.google.common.collect.Iterables.all;
import static org.asoem.greyfish.core.io.GreyfishLogger.GFACTIONS_LOGGER;

public abstract class FiniteStateAction extends AbstractGFAction {

    protected FiniteStateAction(AbstractGFAction.AbstractBuilder<?> builder) {
        super(builder);
    }

    protected FiniteStateAction(FiniteStateAction cloneable, CloneMap cloneMap) {
        super(cloneable, cloneMap);
    }

    protected interface StateAction {
        public Object run();
    }

    /* Immutable after freeze */
    private ImmutableMap<Object, StateAction> states = ImmutableMap.of();
    private Object initialStateName;
    private ImmutableSet<Object> endStateNames = ImmutableSet.of();

    /* Always mutable */
    private Object currentStateName;

    @Override
    protected final void performAction(Simulation simulation) {
        if (endStateNames.contains(currentStateName)) { // TODO: Could be implemented more efficiently via a boolean.
            GFACTIONS_LOGGER.debug("{}: EndTransition to {}", this, initialStateName);
            currentStateName = initialStateName;                                assert currentStateName != null;
        }

        StateAction stateActionToExecute = states.get(currentStateName);     assert stateActionToExecute != null;
        Object nextStateName = stateActionToExecute.run();                   assert nextStateName != null;

        GFACTIONS_LOGGER.debug("{}: Transition to {}", this, nextStateName);

        currentStateName = nextStateName;
    }

    @Override
    protected boolean evaluateInternalState(Simulation simulation) {
        if (states.isEmpty()) {
            GFACTIONS_LOGGER.warn(this + " has no states defined; Execution stopped.");
            return false;
        }

        return true;
    }

    @Override
    public void initialize(Simulation simulation) {
        super.initialize(simulation);
        currentStateName = initialStateName;
    }

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) {
        super.checkConsistency(components);
        if (!states.isEmpty()) {
            checkState(initialStateName != null,
                    "No InitialState defined");
            checkState(states.containsKey(initialStateName),
                    "InitialStatName `"+initialStateName+"' has no actual state in "+states);
            checkState(!endStateNames.isEmpty(),
                    "No EndState defined");
            checkState(all(endStateNames, in(states.keySet())) ,
                    "Not all EndStateNames "+endStateNames+" have an actual state in "+states);
        }
        else
            GFACTIONS_LOGGER.warn("FiniteStateAction has no states defined: " + this);
    }

    @Override
    public final boolean isResuming() {
        return currentStateName != initialStateName && !endStateNames.contains(currentStateName);
    }

    protected final void registerInitialFSMState(final Object state, final StateAction action) {
        checkState(initialStateName == null);
        registerFSMState(state, action);
        initialStateName = state;
    }

    protected final void registerFSMState(final Object state, final StateAction action) {
        states = ImmutableMap.<Object, StateAction>builder().putAll(states).put(state, action).build();
    }

    protected final void registerEndFSMState(final Object state, final StateAction action) {
        registerFSMState(state, action);
        endStateNames = ImmutableSet.<Object>builder().addAll(endStateNames).add(state).build();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + name + "|" + currentStateName + "]@" + getComponentOwner();
    }
}
