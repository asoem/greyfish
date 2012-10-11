package org.asoem.greyfish.utils.collect;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * A List implementation that can hold a fixed number of elements.
 * If the limit is reached, any further addition will replace the the oldest element.
 * Does not permit null values.
 */
public class CircularFifoBuffer<E> extends HookedForwardingList<E> implements Serializable {

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

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (maxSize <= 0)
            throw new InvalidObjectException("box must not be null");
        if (delegate == null)
            throw new InvalidObjectException("delegate must not be null");
    }

    private static final long serialVersionUID = 0;
}
