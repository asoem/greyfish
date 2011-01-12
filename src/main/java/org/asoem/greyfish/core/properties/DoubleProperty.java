package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;

import java.util.Map;

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

	public void subtract(double costs) {
		setValue(value-costs);
	}
	
	public void add(Double double1) {
		setValue(value+double1);
	}
}
