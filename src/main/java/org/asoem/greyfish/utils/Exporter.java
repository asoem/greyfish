package org.asoem.greyfish.utils;


public interface Exporter {
	public <E> void add(ValueAdaptor<E> a);
	public <E> void add(ValueSelectionAdaptor<E> a);
    public <E> void add(MultiValueAdaptor<E> multiValueAdaptor);
}
