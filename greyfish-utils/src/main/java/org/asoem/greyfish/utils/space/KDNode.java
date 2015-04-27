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

import org.asoem.greyfish.utils.collect.BinaryTreeNode;

/**
 * A node of a {@link KDTree}
 */
public interface KDNode<N extends KDNode<N, T>, T> extends BinaryTreeNode<N> {

    /**
     * Get the number of dimensions (value for k).
     *
     * @return the number of dimensions
     */
    int dimensions();

    /**
     * The value stored with this node
     *
     * @return the value stored with this node
     */
    T value();

    /**
     * Get the coordinates for this node.
     *
     * @return the coordinates
     */
    double[] coordinates();

    /**
     * Calculate the distance of this node to the given {@code coordinates}. The length of {@code coordinates} must
     * match the {@link #dimensions() dimension} of this node.
     *
     * @param coordinates the coordinates to calucalte the distance to
     * @return the distance to a point at given {@code coordinates}
     * @throws IllegalArgumentException if the number of given {@code coordinates} does not match the {@link
     *                                  #dimensions() dimension} of this node
     */
    double distance(double... coordinates);
}
