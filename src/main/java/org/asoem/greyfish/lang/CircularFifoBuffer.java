package org.asoem.greyfish.lang;

import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingList;
import javolution.util.FastList;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A List implementation that can hold a fixed number of elements.
 * If the limit is reached, any further addition will replace the the oldest element.
 * Does not permit null values.
 */
public final class CircularFifoBuffer<E> extends ForwardingList<E> {

	private final FastList<E> delegate;
	
	private final int maxSize;
	
	public CircularFifoBuffer(final int size) {
		Preconditions.checkArgument(size > 0);
		this.maxSize = size;
		delegate = new FastList<E>(maxSize);
	}

	/* 
	 * If the buffer is full, the least recently added element is discarded so that a new element can be inserted.
	 */
	public boolean add(E element) {
        checkNotNull(element);
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

    public static <T> CircularFifoBuffer<T> newInstance(int size) {
        return new CircularFifoBuffer<T>(size);
    }

    @Override
    protected List<E> delegate() {
        return delegate;
    }
}
