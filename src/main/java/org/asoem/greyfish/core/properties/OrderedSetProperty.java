package org.asoem.sico.core.properties;

import java.util.Map;

import org.asoem.sico.core.io.GreyfishLogger;
import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.utils.AbstractDeepCloneable;
import org.asoem.sico.utils.Exporter;
import org.asoem.sico.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

import com.jgoodies.validation.ValidationResult;


public abstract class OrderedSetProperty<T extends Comparable<T>> extends AbstractGFProperty implements DiscreteProperty<T> {

	private static final long serialVersionUID = 1L;

	@Element(name="min")
	protected T upperBound;
	
	@Element(name="max")
	protected T lowerBound;
	
	@Element(name="init")
	protected T initialValue;
	
	protected T value;

	public OrderedSetProperty(T min, T max, T init) {
		init(min, max, init);
	}

	protected OrderedSetProperty(OrderedSetProperty<T> property,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(property, mapDict);
		init(property.lowerBound,
				property.upperBound,
				property.initialValue);
	}

	private void init(T min, T max, T init) {
		lowerBound = min;
		upperBound = max;
		initialValue = init;
	}

	public T getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(T upperBound) {
		this.upperBound = upperBound;
	}

	public T getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(T lowerBound) {
		this.lowerBound = lowerBound;
	}

	public T getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(T initialValue) {
		this.initialValue = initialValue;
	}

	@Override
	public T getValue() {
		return value;
	}

	public void setValue(T amount) {
		if (rangeCheck(lowerBound, upperBound, amount)) {
			this.value = amount;
			firePropertyChanged();
		}
		else
			if (GreyfishLogger.isDebugEnabled())
				GreyfishLogger.debug(this.getClass().getSimpleName() + "#setValue("+amount+"): Out of range ("+lowerBound+","+upperBound+")");
	}

	private boolean rangeCheck(T from, T to, T value) {
		return from.compareTo( value ) <= 0
			&& to.compareTo( value ) >= 0;
	}
	
	@Override
	public void initialize(Simulation simulation) {
		super.initialize(simulation);
		setValue(initialValue);
	}
	
	public void export(Exporter e, Class<T> clazz) {
		e.addField(new ValueAdaptor<T>("Min", clazz, getLowerBound()) {

			@Override
			protected void writeThrough(T arg0) {
				setLowerBound(arg0);
			}
		});
		e.addField(new ValueAdaptor<T>("Max", clazz, getUpperBound()) {

			@Override
			protected void writeThrough(T arg0) {
				setUpperBound(arg0);
			}
		});
		e.addField(new ValueAdaptor<T>("Initial", clazz, getInitialValue()) {

			@Override
			protected void writeThrough(T arg0) {
				setInitialValue(arg0);
			}
			@Override
			public ValidationResult validate() {
				ValidationResult validationResult = new ValidationResult();
				if (!rangeCheck(getLowerBound(), getUpperBound(), getInitialValue()))
					validationResult.addError("Value of `Initial' must not be smaller than `Min' and greater than `Max'");
				return validationResult;
			}
		});
	}
}
