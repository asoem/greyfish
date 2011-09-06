package org.asoem.greyfish.utils;

public interface ConfigurationHandler {
	public <E> void add(ValueAdaptor<E> a);
	public <E> void add(FiniteSetValueAdaptor<E> a);
    public <E> void add(MapValuesAdaptor<E> multiValueAdaptor);
}
