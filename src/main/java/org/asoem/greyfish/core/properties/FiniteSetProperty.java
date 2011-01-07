package org.asoem.greyfish.core.properties;

public interface FiniteSetProperty<T> extends DiscreteProperty<T> {

	public T[] getSet();
	
}
