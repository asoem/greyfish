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

/**
 * An interface for collections which want to support search operations directly,
 * without the need to use the possibly less efficient static methods in {@link com.google.common.collect.Iterators},
 * {@link com.google.common.collect.Iterables}, {@link com.google.common.collect.Collections2} or {@code Lists}.
 * @deprecated All method definitions will be moved to {@link FunctionalIterable}.
 */
@Deprecated
public interface Searchable<E> extends Iterable<E> {
    /**
     * Find the first element which satisfies {@code predicate}.
     * @param predicate the predicate to check the element against
     * @return An {@code Optional} holding the element which was found, or {@link Optional#absent()}.
     */
    Optional<E> findFirst(Predicate<? super E> predicate);

    /**
     * Filter all elements by given the {@code predicate}.
     * @param predicate the {@code Predicate} to check the elements against
     * @return All elements which satisfy {@code predicate}
     */
    Iterable<E> filter(Predicate<? super E> predicate);
}
