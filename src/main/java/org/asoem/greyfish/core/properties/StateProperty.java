package org.asoem.sico.core.properties;

import java.util.Map;

import org.asoem.sico.lang.ClassGroup;
import org.asoem.sico.utils.AbstractDeepCloneable;
import org.asoem.sico.utils.State;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementArray;

@ClassGroup(tags="property")
public class StateProperty extends AbstractDiscreteProperty<State> {

	private static final long serialVersionUID = -5080471457682141219L;

	private static final State[] NULL_STATES = new State[0];

	@Element(name="initialState")
	protected State parameterInitialState;

	@ElementArray(name="states")
	protected State[] parameterStates = NULL_STATES;

	public StateProperty() {
	}

	protected StateProperty(StateProperty individualProperty,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(individualProperty, mapDict);

		parameterStates = individualProperty.parameterStates;
		parameterInitialState = individualProperty.parameterInitialState;
	}

	public State[] getParameterStates() {
		return parameterStates;
	}

	public void setParameterStates(State[] parameterStates) {
		this.parameterStates = (parameterStates.length == 0) ? NULL_STATES : parameterStates;
	}

	public State getParameterInitialState() {
		return parameterInitialState;
	}

	public void setParameterInitialState(State parameterInitialState) {
		this.parameterInitialState = parameterInitialState;
	}

	public State[] valuesParameterInitialState() {
		return parameterStates;
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new StateProperty(this, mapDict);
	}
}
