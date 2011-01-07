package org.asoem.greyfish.core.conditions;

import java.util.Map;

import org.asoem.greyfish.core.properties.IntProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

public class IntPropertyCondition extends IntCompareCondition {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5309355739071313965L;

	@Element(name="property")
	private IntProperty parameterQuantitiveProperty;

	public IntPropertyCondition(IntPropertyCondition condition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(condition, mapDict);
		final IntProperty parameterQuantitiveProperty = deepClone(condition.getParameterQuantitiveProperty(), mapDict);
		setParameterQuantitiveProperty(parameterQuantitiveProperty);
	}

	public IntProperty getParameterQuantitiveProperty() {
		return parameterQuantitiveProperty;
	}

	public void setParameterQuantitiveProperty(IntProperty quantitiveProperty) {
		this.parameterQuantitiveProperty = quantitiveProperty;
	}

	public IntProperty[] valuesParameterQuantitiveProperty() {
		return getComponentOwner().getProperties(IntProperty.class).toArray(new IntProperty[0]);
	}
	
	@Override
	public void export(Exporter e) {
		super.export(e);
		e.addField(new ValueSelectionAdaptor<IntProperty>("", IntProperty.class, parameterQuantitiveProperty, getComponentOwner().getProperties(IntProperty.class)) {

			@Override
			protected void writeThrough(IntProperty arg0) {
				parameterQuantitiveProperty = arg0;
			}
		});
	}

	@Override
	public void initialize(Simulation simulation) {
		super.initialize(simulation);
		if (parameterQuantitiveProperty == null) {
			parameterQuantitiveProperty = new IntProperty();
			getComponentOwner().addProperty(parameterQuantitiveProperty);
		}
	}

	@Override
	protected Integer getCompareValue(Simulation simulation) {
		return parameterQuantitiveProperty.getValue().intValue();
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new IntPropertyCondition(this, mapDict);
	}
}
