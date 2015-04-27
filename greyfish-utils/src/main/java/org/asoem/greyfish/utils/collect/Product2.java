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

/**
 * A generic interface for objects which encapsulate a pair of values. <p><b>Don't overuse this interface!</b> For a
 * matter of readability you should consider to write your own pair class if you don't use any utility function
 * associated with it (see {@link org.asoem.greyfish.utils.collect.Products}) or add methods with names of a defined
 * meaning to the implementation which delegate to the generic accessor functions.</p>
 *
 * @param <E1> the type of the first value
 * @param <E2> the type of the second value
 */
public interface Product2<E1, E2> {
    /**
     * Get the first value
     *
     * @return the first value
     */
    E1 first();

    /**
     * Get the second value
     *
     * @return the second value
     */
    E2 second();
}
