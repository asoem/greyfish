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

import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is a {@link org.asoem.greyfish.utils.collect.FunctionalList FunctionalList} implementation for lists of size 6.
 * The predictable size allows some operations to unroll the list for performance optimizations.
 *
 * @param <E> the element type
 */
final class UnrolledList6<E> extends DelegatingImmutableFunctionalList<E> implements Serializable {

    private final List<E> delegate;

    UnrolledList6(final Iterable<? extends E> elements) {
        checkNotNull(elements);
        this.delegate = ImmutableList.copyOf(elements);
        checkArgument(delegate.size() == 6);
    }

    @Override
    protected List<E> delegate() {
        return delegate;
    }

    @Override
    public Optional<E> findFirst(final Predicate<? super E> predicate) {
        checkNotNull(predicate, "Predicate is null");
        final E e0 = delegate.get(0);
        if (predicate.apply(e0)) {
            return Optional.of(e0);
        }
        final E e1 = delegate.get(1);
        if (predicate.apply(e1)) {
            return Optional.of(e1);
        }
        final E e2 = delegate.get(2);
        if (predicate.apply(e2)) {
            return Optional.of(e2);
        }
        final E e3 = delegate.get(3);
        if (predicate.apply(e3)) {
            return Optional.of(e3);
        }
        final E e4 = delegate.get(4);
        if (predicate.apply(e4)) {
            return Optional.of(e4);
        }
        final E e5 = delegate.get(5);
        if (predicate.apply(e5)) {
            return Optional.of(e5);
        }
        return Optional.absent();
    }
}
