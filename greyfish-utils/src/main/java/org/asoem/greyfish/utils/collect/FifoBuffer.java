/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.utils.collect;

import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingCollection;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A List implementation that can hold a fixed number of elements.
 * If the limit is reached, any further addition will replace the the oldest element.
 * Does not permit null values.
 */
public final class FifoBuffer<E> extends ForwardingCollection<E> implements Serializable {

    private final LinkedList<E> delegate;
    private final int capacity;

    public FifoBuffer(final int size) {
        Preconditions.checkArgument(size > 0);
        this.capacity = size;
        delegate = new LinkedList<E>();
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isFull() {
        return size() == capacity();
    }

    public int capacity() {
        return capacity;
    }

    @Override
    protected List<E> delegate() {
        return delegate;
    }

    @Override
    public boolean add(final E element) {
        synchronized (this) {
            final boolean add = super.add(element);
            if (size() > capacity) {
                delegate.removeFirst();
            }
            return add;
        }
    }

    @Override
    public boolean addAll(@Nonnull final Collection<? extends E> collection) {
        return standardAddAll(collection);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FifoBuffer)) {
            return false;
        }

        final FifoBuffer that = (FifoBuffer) o;

        if (capacity != that.capacity) {
            return false;
        }
        if (!delegate.equals(that.delegate)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = delegate.hashCode();
        result = 31 * result + capacity;
        return result;
    }

    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (capacity <= 0) {
            throw new InvalidObjectException("box must not be null");
        }
    }

    private static final long serialVersionUID = 0;

    public static <T> FifoBuffer<T> newInstance(final int size) {
        return new FifoBuffer<T>(size);
    }
}
