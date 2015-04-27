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

import com.google.common.base.Predicate;

import java.util.Collection;
import java.util.List;

/**
 * A {@link Collection} which supports functional style operations.
 */
public interface FunctionalCollection<E> extends Collection<E>, Searchable<E> {
    /**
     * Check if at least one of the elements satisfies the given {@code predicate}.
     *
     * @param predicate the {@code Predicate} to check against
     * @return {@code true} if at least one element satisfies {@code predicate}, {@code false} otherwise
     */
    boolean any(Predicate<E> predicate);

    /**
     * Get all elements satisfying the predicate and remove them from the collection
     *
     * @param predicate The predicate a message has to satisfy
     * @return All messages that satisfy the predicate
     */
    List<E> remove(Predicate<? super E> predicate);
}
