package org.asoem.sico.core.conditions;

import java.util.Map;

import org.asoem.sico.core.properties.DoubleProperty;
import org.asoem.sico.core.properties.IntProperty;
import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.utils.AbstractDeepCloneable;
import org.asoem.sico.utils.Exporter;
import org.asoem.sico.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

public class DoublePropertyCondition extends DoubleCompareCondition {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5309355739071313965L;

	@Element(name="property")
	private DoubleProperty parameterQuantitiveProperty;

	public DoublePropertyCondition() {
	}

	public DoublePropertyCondition(DoublePropertyCondition condition,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(condition, mapDict);
		final DoubleProperty parameterQuantitiveProperty = deepClone(condition.getParameterQuantitiveProperty(), mapDict);
		setParameterQuantitiveProperty(parameterQuantitiveProperty);
	}

	public DoubleProperty getParameterQuantitiveProperty() {
		return parameterQuantitiveProperty;
	}

	public void setParameterQuantitiveProperty(DoubleProperty quantitiveProperty) {
		this.parameterQuantitiveProperty = quantitiveProperty;
	}

	public IntProperty[] valuesParameterQuantitiveProperty() {
		return getComponentOwner().getProperties(IntProperty.class).toArray(new IntProperty[0]);
	}
	
	@Override
	public void export(Exporter e) {
		super.export(e);
		e.addField(new ValueSelectionAdaptor<DoubleProperty>("", DoubleProperty.class, parameterQuantitiveProperty, getComponentOwner().getProperties(DoubleProperty.class)) {

			@Override
			protected void writeThrough(DoubleProperty arg0) {
				parameterQuantitiveProperty = arg0;
			}
		});
	}

	@Override
	public void initialize(Simulation simulation) {
		super.initialize(simulation);
		if (parameterQuantitiveProperty == null) {
			parameterQuantitiveProperty = new DoubleProperty();
			getComponentOwner().addProperty(parameterQuantitiveProperty);
		}
	}

	@Override
	protected Double getCompareValue(Simulation simulation) {
		return parameterQuantitiveProperty.getValue().doubleValue();
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new DoublePropertyCondition(this, mapDict);
	}
}
