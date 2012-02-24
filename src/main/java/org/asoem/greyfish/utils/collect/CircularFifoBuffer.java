package org.asoem.greyfish.utils.collect;

import com.google.common.base.Preconditions;
import javolution.util.FastList;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A List implementation that can hold a fixed number of elements.
 * If the limit is reached, any further addition will replace the the oldest element.
 * Does not permit null values.
 */
public class CircularFifoBuffer<E> extends HookedForwardingList<E> {

	private final FastList<E> delegate;
	
	private final int maxSize;
	
	public CircularFifoBuffer(final int size) {
		Preconditions.checkArgument(size > 0);
		this.maxSize = size;
		delegate = new FastList<E>(maxSize);
	}
	
	public boolean isFull() {
		return delegate.size() == maxSize;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CircularFifoBuffer that = (CircularFifoBuffer) o;

        if (maxSize != that.maxSize) return false;
        if (!delegate.equals(that.delegate)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + delegate.hashCode();
        result = 31 * result + maxSize;
        return result;
    }
}
