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

import java.io.InvalidObjectException;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkElementIndex;

final class EmptyFunctionalList extends AbstractFunctionalList<Object> implements Serializable {
    private static final transient EmptyFunctionalList INSTANCE = new EmptyFunctionalList();

    private EmptyFunctionalList() {
    }

    @Override
    public Object get(final int index) {
        checkElementIndex(index, 0);
        throw new AssertionError("unreachable");
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Optional<Object> findFirst(final Predicate<? super Object> predicate) {
        return Optional.absent();
    }

    @Override
    public Iterable<Object> filter(final Predicate<? super Object> predicate) {
        return this;
    }

    private Object readResolve() throws InvalidObjectException {
        return INSTANCE;
    }

    private static final long serialVersionUID = 0;

    @SuppressWarnings("unchecked")
    public static <E> FunctionalList<E> instance() {
        return (FunctionalList<E>) INSTANCE;
    }
}
