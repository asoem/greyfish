package org.asoem.greyfish.utils.collect;

import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.ForwardingListIterator;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * A List implementation that can hold a fixed number of elements.
 * If the limit is reached, any further addition will replace the the oldest element.
 * Does not permit null values.
 */
public class CircularFifoBuffer<E> extends ForwardingList<E> implements Serializable {

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
    public boolean add(E element) {
        return standardAdd(element);
    }

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        trim();
    }

    private void trim() {
        while (size() > maxSize)
            delegate.removeFirst();
    }

    @Override
    public boolean addAll(@Nonnull Collection<? extends E> collection) {
        return standardAddAll(collection);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> elements) {
        final boolean ret = super.addAll(index, elements);
        trim();
        return ret;
    }

    @Override
    public @Nonnull ListIterator<E> listIterator() {
        return standardListIterator();
    }

    @Override
    public @Nonnull ListIterator<E> listIterator(final int index) {
        return new ForwardingListIterator<E>() {

            private ListIterator<E> listIterator = delegate.listIterator(index);

            @Override
            protected ListIterator<E> delegate() {
                return listIterator;
            }

            @Override
            public void add(E element) {
                throw new UnsupportedOperationException();
            }
        };
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
