package org.asoem.greyfish.core.properties;


import com.google.common.base.Supplier;

public abstract interface DiscreteProperty<T> extends GFProperty {

	public T getValue();
}
