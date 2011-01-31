package org.asoem.greyfish.utils;

import com.google.common.collect.Iterables;
import com.jgoodies.binding.list.SelectionInList;

import java.util.List;


public abstract class ValueSelectionAdaptor<T> extends ValueAdaptor<T> {

	public final SelectionInList<T> inList;
	
	public ValueSelectionAdaptor(final String name, Class<T> clazz, final T o, final T[] values) {
		super(name, clazz, o);
		inList = new SelectionInList<T>(values);
	}

	public ValueSelectionAdaptor(final String name, Class<T> clazz, final T o, final List<T> values) {
		super(name, clazz, o);
		inList = new SelectionInList<T>(values);
	}
	
	public ValueSelectionAdaptor(final String name, Class<T> clazz, final T o, final Iterable<T> values) {
		super(name, clazz, o);
		final T[] list = Iterables.toArray(values, clazz);
		inList = new SelectionInList<T>(list);
	}
}
