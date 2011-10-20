package org.asoem.greyfish.utils.collect;

import javolution.util.FastList;

import java.util.AbstractQueue;
import java.util.Iterator;

public class FastQueue<E> extends AbstractQueue<E> {

	private final FastList<E> list = new FastList<E>();
	
	public FastQueue() {
	}

	@Override
	public boolean offer(E arg0) {
		return list.add(arg0);
	}

	@Override
	public E peek() {
		if (list.isEmpty())
			return null;
		return list.getFirst();
	}

	@Override
	public E poll() {
		if (list.isEmpty())
			return null;
		return list.removeFirst();
	}

	@Override
	public Iterator<E> iterator() {
		return list.iterator();
	}

	@Override
	public int size() {
		return list.size();
	}
}
