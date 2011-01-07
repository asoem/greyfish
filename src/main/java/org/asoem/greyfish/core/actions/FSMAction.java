package org.asoem.greyfish.core.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

import com.google.common.base.Preconditions;

public abstract class FSMAction extends AbstractGFAction {

	protected abstract interface StateAction {
		public String action();
	}
	
	private HashMap<String, StateAction> states = new HashMap<String, StateAction>();
	
	private String initialStateName;
	
	private Collection<String> endStates = new ArrayList<String>();
	
	private String currentStateName;
	
	public FSMAction() {
	}
	
	public FSMAction(String name) {
		super(name);
	}

	public FSMAction(FSMAction action,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(action, mapDict);
		initialStateName = action.initialStateName;
		endStates.addAll(action.endStates);
		states.putAll(action.states);
	}

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
}
