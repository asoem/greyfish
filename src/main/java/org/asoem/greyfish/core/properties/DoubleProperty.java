package org.asoem.sico.core.properties;

import java.util.Map;

import org.asoem.sico.lang.ClassGroup;
import org.asoem.sico.utils.AbstractDeepCloneable;
import org.asoem.sico.utils.Exporter;

@ClassGroup(tags="property")
public class DoubleProperty extends OrderedSetProperty<Double> {
	
	public DoubleProperty() {
		super(0.0, 100.0, 100.0);
	}

	protected DoubleProperty(DoubleProperty doubleProperty,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(doubleProperty, mapDict);
	}
	
	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new DoubleProperty(this, mapDict);
	}
	
	@Override
	public void export(Exporter e) {
		super.export(e, Double.class);
	}

	public void substract(double costs) {
		setValue(value-costs);
	}
	
	public void add(Double double1) {
		setValue(value+double1);
	}
}
