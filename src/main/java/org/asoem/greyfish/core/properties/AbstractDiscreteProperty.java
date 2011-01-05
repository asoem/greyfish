package org.asoem.sico.core.properties;

import java.util.Map;

import org.asoem.sico.utils.AbstractDeepCloneable;


public abstract class AbstractDiscreteProperty<T> extends AbstractGFProperty implements DiscreteProperty<T> {

	private static final long serialVersionUID = 2723870674494281995L;

	protected T value;

	public AbstractDiscreteProperty() {
	}

	public AbstractDiscreteProperty(AbstractDiscreteProperty<T> property,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(property, mapDict);
	}

	@Override
	public T getValue() {
		return value;
	}
	
//	@Override
//	public String toString() {
//		return new String(super.toString() + " (item=" + String.valueOf(item) + ")");
//	}
}
