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
import com.google.common.collect.Iterables;

/**
 * This is a utility class which has the same methods as {@link Iterables} but is aware of the direct implementation in
 * functional collections such a {@link org.asoem.greyfish.utils.collect.FunctionalCollection}. That means this class
 * decides weather to delegates to the iterables native methods or the helper methods in {@link Iterables} based on the
 * type of the subject.
 *
 * @deprecated Will be removed
 */
@Deprecated
public class Functionals {
    private Functionals() {
    }

    /**
     * @deprecated Will be removed
     */
    @Deprecated
    @SuppressWarnings("unchecked") // safe cast
    public static <E> Optional<E> tryFind(final Iterable<E> iterable, final Predicate<? super E> predicate) {
        if (iterable instanceof Searchable) {
            return ((Searchable<E>) iterable).findFirst(predicate);
        } else {
            return Iterables.tryFind(iterable, predicate);
        }
    }
}
