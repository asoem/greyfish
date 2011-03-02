package org.asoem.greyfish.core.actions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.CloneMap;

import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Predicates.in;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.all;
import static org.asoem.greyfish.core.io.GreyfishLogger.GFACTIONS_LOGGER;

public abstract class FiniteStateAction extends AbstractGFAction {

    protected FiniteStateAction(AbstractGFAction.AbstractBuilder<?> builder) {
        super(builder);
    }

    protected FiniteStateAction(FiniteStateAction cloneable, CloneMap cloneMap) {
        super(cloneable, cloneMap);
    }

    protected abstract interface StateAction {
        public String action();
    }

    /* Immutable after freeze */
    private ImmutableMap<String, StateAction> states = ImmutableMap.of();
    private String initialStateName;
    private ImmutableSet<String> endStateNames = ImmutableSet.of();

    /* Always mutable */
    private String currentStateName;

    @Override
    protected final void performAction(Simulation simulation) {
        if (endStateNames.contains(currentStateName)) { // TODO: Could be implemented more efficiently via a boolean.
            if (GFACTIONS_LOGGER.hasTraceEnabled())
                GFACTIONS_LOGGER.trace(this + ": EndTransition to " + initialStateName);
            currentStateName = initialStateName;                                assert currentStateName != null;
        }

        StateAction stateActionToExecute = states.get(currentStateName);        assert stateActionToExecute != null;
        String nextStateName = stateActionToExecute.action();                   assert nextStateName != null;

        if (GFACTIONS_LOGGER.hasTraceEnabled())
            GFACTIONS_LOGGER.trace(this + ": Transition to " + nextStateName);

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
    public final boolean done() {
        return endStateNames.contains(currentStateName);
    }

    protected final void registerInitialFSMState(final String state, final StateAction action) {
        checkState(isNullOrEmpty(initialStateName));
        registerFSMState(state, action);
        initialStateName = state;
    }

    protected final void registerFSMState(final String state, final StateAction action) {
        checkArgument(!isNullOrEmpty(state));
        checkNotNull(action); // Redundant: ImmutableMap doesn't allow null keys or values
        states = ImmutableMap.<String, StateAction>builder().putAll(states).put(state, action).build();
    }

    protected final void registerEndFSMState(final String state, final StateAction action) {
        registerFSMState(state, action);
        endStateNames = ImmutableSet.<String>builder().addAll(endStateNames).add(state).build();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "('" + name + "'@" + currentStateName + ")";
    }
}
