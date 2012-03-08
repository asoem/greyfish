package org.asoem.greyfish.utils.collect;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

/**
 * A List implementation that can hold a fixed number of elements.
 * If the limit is reached, any further addition will replace the the oldest element.
 * Does not permit null values.
 */
public class CircularFifoBuffer<E> extends HookedForwardingList<E> {

	private final LinkedList<E> delegate;

	private final int maxSize;

	public CircularFifoBuffer(final int size) {
		Preconditions.checkArgument(size > 0);
		this.maxSize = size;
		delegate = new LinkedList<E>();
	}
	
	@SuppressWarnings("UnusedDeclaration")
    public boolean isFull() {
		return size() == maxSize();
	}
	
	public int maxSize() {
		return maxSize;
	}

    public static <T> CircularFifoBuffer<T> newInstance(int size) {
        return new CircularFifoBuffer<T>(size);
    }

    @Override
    protected List<E> delegate() {
        return delegate;
    }

    @Override
    protected final void afterAddition(@Nullable E element, int index) {
        if (size() > maxSize)
            delegate.removeFirst();
    }
}
