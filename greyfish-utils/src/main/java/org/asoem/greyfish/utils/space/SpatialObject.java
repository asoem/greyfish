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
 * Interface for objects in a vector space.
 */
public interface SpatialObject {

    /**
     * Get the dimension of the object coordinates.
     *
     * @return the dimension of the coordinates defining this object
     */
    int getDimension();

    /**
     * Get the centroid for this shape. <p/> <p>A centroid is defined as the mean position of all the points in all of
     * the coordinate directions.</p>
     *
     * @return the centroid of this object
     */
    Point getCentroid();

}
