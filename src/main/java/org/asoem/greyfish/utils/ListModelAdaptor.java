package org.asoem.greyfish.utils;

import javax.swing.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ListModelAdaptor<T> extends AbstractListModel {

	private static final long serialVersionUID = -8856005640394691336L;

	private List<T> collection;

	public ListModelAdaptor(List<T> collection) {
		this.collection = collection;
	}

	@Override
	public Object getElementAt(int index) {
		return collection.get(index);
	}

	@Override
	public int getSize() {
		return collection.size();
	}

	public void add(int index, T element) {
		collection.add(index, element);
		fireIntervalAdded(this, index, index);
	}

	public boolean add(T e) {
		if( collection.add(e) ) {
			fireIntervalAdded(this, collection.size() - 1, collection.size() - 1);
			return true;
		}
		else
			return false;
	}

	public boolean addAll(Collection<? extends T> c) {
		return addAll(collection.size(), c);
	}

	public boolean addAll(int index, Collection<? extends T> c) {
		if( collection.addAll(index, c) ) {
			fireIntervalAdded(this, index, index + c.size() - 1);
			return true;
		}
		else
			return false;
	}

	public void clear() {
		final int size = size();
		collection.clear();
		fireIntervalRemoved(this, 0, size - 1);
	}

	public boolean contains(Object o) {
		return collection.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return collection.containsAll(c);
	}

	public T get(int index) {
		return collection.get(index);
	}

	public int indexOf(Object o) {
		return collection.indexOf(o);
	}

	public boolean isEmpty() {
		return collection.isEmpty();
	}

	public Iterator<T> iterator() {
		return collection.iterator();
	}

	public int lastIndexOf(Object o) {
		return collection.lastIndexOf(o);
	}

	public ListIterator<T> listIterator() {
		return collection.listIterator();
	}

	public ListIterator<T> listIterator(int index) {
		return collection.listIterator(index);
	}

	public T remove(int index) {
		return collection.remove(index);
	}

	public boolean remove(Object o) {
		final int index = indexOf(o);
		if( collection.remove(o) ) {
			fireIntervalRemoved(this, index, index);
			return true;
		}
		else
			return false;
	}

	public T set(int index, T element) {
		T ret = collection.set(index, element);
		fireContentsChanged(this, index, index);
		return ret;
	}

	public int size() {
		return collection.size();
	}

	public List<T> subList(int fromIndex, int toIndex) {
		return collection.subList(fromIndex, toIndex);
	}

	public Object[] toArray() {
		return collection.toArray();
	}

	public T[] toArray(T[] a) {
		return collection.toArray(a);
	}
}
