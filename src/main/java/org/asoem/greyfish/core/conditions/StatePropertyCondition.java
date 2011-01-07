package org.asoem.greyfish.core.conditions;

import java.util.Map;

import org.asoem.greyfish.core.properties.FiniteSetProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

public class StatePropertyCondition extends LeafCondition {
	
	@Element(name="property",required=false)
	private FiniteSetProperty<?> parameterStateProperty;
	
	@Element(name="state",required=false)
	private Object parameterState;

	public StatePropertyCondition() {
	}

	public StatePropertyCondition(
			StatePropertyCondition condition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(condition, mapDict);
		parameterStateProperty = deepClone(condition.parameterStateProperty, mapDict);
		parameterState = condition.parameterState;
	}

	@Override
	public boolean evaluate(Simulation simulation) {
		return parameterStateProperty != null && 
			parameterStateProperty.getValue().equals(parameterState);
	}

	@Override
	public void initialize(Simulation simulation) {
		super.initialize(simulation);
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new StatePropertyCondition(this, mapDict);
	}
	
	@Override
	public void export(Exporter e) {
		e.addField(new ValueSelectionAdaptor<FiniteSetProperty>("", FiniteSetProperty.class, parameterStateProperty, getComponentOwner().getProperties(FiniteSetProperty.class)) {
			@Override
			protected void writeThrough(FiniteSetProperty arg0) {
				StatePropertyCondition.this.parameterStateProperty = arg0;
			}
		});
		e.addField(new ValueSelectionAdaptor<Object>("has state", Object.class, parameterState, (parameterStateProperty == null) ? new Object[0] : parameterStateProperty.getSet()) {
			@Override
			protected void writeThrough(Object arg0) {
				StatePropertyCondition.this.parameterState = arg0;
			}
		});
	}
}
