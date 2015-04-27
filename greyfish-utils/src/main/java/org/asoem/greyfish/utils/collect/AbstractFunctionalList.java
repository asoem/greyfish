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
import com.google.common.collect.Iterables;

import java.util.AbstractList;
import java.util.List;

/**
 * Abstract base class for 'functional' lists.
 */
abstract class AbstractFunctionalList<E> extends AbstractList<E> implements FunctionalList<E> {

    @Override
    public Iterable<E> filter(final Predicate<? super E> predicate) {
        return Iterables.filter(this, predicate);
    }

    @Override
    public boolean any(final Predicate<E> predicate) {
        return Iterables.any(this, predicate);
    }

    @Override
    public Optional<E> findFirst(final Predicate<? super E> predicate) {
        return Iterables.tryFind(this, predicate);
    }

    @Override
    public List<E> remove(final Predicate<? super E> predicate) {
        final ImmutableList<E> remove = ImmutableList.copyOf(filter(predicate));
        Iterables.removeAll(this, remove);
        return remove;
    }
}
