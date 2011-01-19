package org.asoem.greyfish.core.actions;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.simulation.Simulation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class FSMAction extends AbstractGFAction {

    protected FSMAction(AbstractBuilder<?> builder) {
        super(builder);
    }

    protected abstract interface StateAction {
        public String action();
    }

    private Map<String, StateAction> states = new HashMap<String, StateAction>();

    private String initialStateName;

    private Set<String> endStates = new HashSet<String>();

    private String currentStateName;

    @Override
    protected final void performAction(Simulation simulation) {
        if (done()) {
            currentStateName = initialStateName;
        }
        StateAction action = states.get(currentStateName);
        String oldStateName = currentStateName;
        currentStateName = action.action();

        if (GreyfishLogger.isTraceEnabled())
            GreyfishLogger.trace(this + ": t("+oldStateName+" => "+currentStateName+")");
    }

    @Override
    public void initialize(Simulation simulation) {
        super.initialize(simulation);
        states = ImmutableMap.copyOf(states);
        endStates = ImmutableSet.copyOf(endStates);
        currentStateName = initialStateName;
    }

    @Override
    public final boolean done() {
        return endStates.contains(currentStateName);
    }

    protected final void registerInitialState(final String state, final StateAction action) {
        initialStateName = state;
        registerState(state, action);
    }

    protected final void registerState(final String state, final StateAction action) {
        Preconditions.checkNotNull(state);
        Preconditions.checkNotNull(action);
        states.put(state, action);
    }

    protected final void registerEndState(final String state, final StateAction action) {
        registerState(state, action);
        endStates.add(state);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "name='" + name + "\', " +
                "state='" + currentStateName + '\'' +
                '}';
    }
}
