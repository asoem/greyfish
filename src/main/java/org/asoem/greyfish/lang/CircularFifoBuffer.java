package org.asoem.greyfish.lang;

import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingCollection;
import javolution.util.FastList;

import java.util.Collection;

public class CircularFifoBuffer<E> extends ForwardingCollection<E> {

	private final FastList<E> delegate;
	
	private final int maxSize;
	
	public CircularFifoBuffer() {
		this.maxSize = 32;
		delegate = new FastList<E>(maxSize);
	}
	
	public CircularFifoBuffer(final int size) {
		Preconditions.checkArgument(size > 0);
		this.maxSize = size;
		delegate = new FastList<E>(maxSize);
	}
	
	@Override
	protected Collection<E> delegate() {
		return delegate;
	}
	
	/* 
	 * If the buffer is full, the least recently added element is discarded so that a new element can be inserted.
	 */
	public boolean add(E element) {
		if (isFull()) {
			final E removed = delegate.removeFirst();
			elementReplaced(removed);
		}
		delegate.addLast(element);
		return true;
	}

    @Override
	public boolean addAll(Collection<? extends E> collection) {
		for (E e : collection) {
			add(e);
		}
		return true;
	}
	
	public boolean isFull() {
		return delegate.size() == maxSize;
	}
	
	public int maxSize() {
		return maxSize;
	}
	
	public void elementReplaced(E element) {}
}
