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

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is a {@link org.asoem.greyfish.utils.collect.FunctionalList FunctionalList} implementation for lists of size 1.
 * The predictable size allows some operations to unroll the list for performance optimizations.
 *
 * @param <E> the element type
 */
final class UnrolledList1<E> extends ImmutableFunctionalList<E> implements Serializable {
    private final E element;

    UnrolledList1(final E element) {
        this.element = checkNotNull(element);
    }

    @Override
    public E get(final int index) {
        checkElementIndex(index, size());
        return element;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Iterable<E> filter(final Predicate<? super E> predicate) {
        return checkNotNull(predicate).apply(element) ? this : ImmutableList.<E>of();
    }

    @Override
    public boolean any(final Predicate<E> predicate) {
        return checkNotNull(predicate).apply(element);
    }

    @Override
    public Optional<E> findFirst(final Predicate<? super E> predicate) {
        return checkNotNull(predicate).apply(element) ? Optional.of(element) : Optional.<E>absent();
    }

    private void readObject(final ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (element == null) {
            throw new InvalidObjectException("Class does not accept null values");
        }
    }
}
