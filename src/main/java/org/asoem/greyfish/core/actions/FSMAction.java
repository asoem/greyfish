package org.asoem.greyfish.core.actions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.simulation.Simulation;

import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Predicates.in;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.all;

public abstract class FSMAction extends AbstractGFAction {

    protected FSMAction(AbstractGFAction.AbstractBuilder<?> builder) {
        super(builder);
    }

    protected FSMAction(FSMAction cloneable, CloneMap cloneMap) {
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
        if (GreyfishLogger.isTraceEnabled())
            GreyfishLogger.trace("FSM: " + this);

        if (done()) {
            currentStateName = initialStateName;
        }
        StateAction action = states.get(currentStateName);
        String oldStateName = currentStateName;
        currentStateName = action.action();

        if (GreyfishLogger.isTraceEnabled())
            GreyfishLogger.trace("FSM: " + this + ": t("+oldStateName+" => "+currentStateName+")");
    }

    @Override
    public boolean evaluate(Simulation simulation) {
        return super.evaluate(simulation)
                && !states.isEmpty();
    }

    @Override
    public void initialize(Simulation simulation) {
        super.initialize(simulation);
        currentStateName = initialStateName;
    }

    @Override
    public void checkIfFreezable(Iterable<? extends GFComponent> components) {
        super.checkIfFreezable(components);
        if (!states.isEmpty()) {
            checkState(initialStateName != null, "No InitialState defined");
            checkState(states.containsKey(initialStateName), "InitialStatName `"+initialStateName+"' has no actual state in "+states);
            checkState(!endStateNames.isEmpty(), "No EndState defined");
            checkState(all(endStateNames, in(states.keySet())) , "Not all EndStateNames "+endStateNames+" have an actual state in "+states);
        }
        else
            GreyfishLogger.warn("FSMAction has no states defined: " + this);
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
        checkNotNull(action);
        states = ImmutableMap.<String, StateAction>builder().putAll(states).put(state, action).build();
    }

    protected final void registerEndFSMState(final String state, final StateAction action) {
        registerFSMState(state, action);
        endStateNames = ImmutableSet.<String>builder().addAll(endStateNames).add(state).build();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "name='" + name + "\', " +
                "state='" + currentStateName + '\'' +
                '}';
    }
}
