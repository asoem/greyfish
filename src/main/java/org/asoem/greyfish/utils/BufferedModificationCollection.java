package org.asoem.greyfish.utils;

import java.util.Collection;
import java.util.List;

import javolution.util.FastList;

import com.google.common.collect.ForwardingCollection;

public class BufferedModificationCollection<T> extends ForwardingCollection<T> {

	private final FastList<T> objects = new FastList<T>();
	private final FastList<T> incomingObjects = new FastList<T>();
	private final FastList<T> outgoingObjects = new FastList<T>();

	public BufferedModificationCollection<T> commitChanges() {
		objects.removeAll(outgoingObjects);
		objects.addAll(incomingObjects);

		outgoingObjects.clear();
		incomingObjects.clear();

		return this;
	}

	@Override
	public boolean add(T e) {
		return incomingObjects.add(e);
	};

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return incomingObjects.addAll(c);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		return contains(o) ? outgoingObjects.add((T) o) : false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean removeAll(Collection<?> c) {
		boolean ret = true;
		for (Object object : c) {
			ret &= contains(object);
		}
		if (ret)
			for (Object object : c) {
				outgoingObjects.add((T) object);
			}
		return ret;
	}

	@Override
	public void clear() {
		outgoingObjects.clear();
		outgoingObjects.addAll(this);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		for (T element : this) {
			if (! c.contains(element))
				outgoingObjects.add(element);
		}
		return true;
	}

	@Override
	protected List<T> delegate() {
		return objects;
	}

	public FastList.Node<T> head() {
		return objects.head();
	}

	public FastList.Node<T> tail() {
		return objects.tail();
	}
}
