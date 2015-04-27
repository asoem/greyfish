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

package org.asoem.greyfish.utils.space;

/**
 * Interface for points in a vector space.
 */
public interface Point extends SpatialObject {

    /**
     * Get the coordinates of the point.
     *
     * @return the coordinates of the point
     */
    double[] coordinates();

    /**
     * Get the coordinate at the given {@code index}.
     *
     * @param index the index index
     * @return the coordinate at given {@code index}
     * @throws java.lang.IndexOutOfBoundsException
     */
    double get(int index);

    /**
     * Calculate the Euclidean distance to given {@code point}.
     *
     * @param point the remote point
     * @return the Euclidean distance to {@code point}
     */
    double distance(Point point);
}
