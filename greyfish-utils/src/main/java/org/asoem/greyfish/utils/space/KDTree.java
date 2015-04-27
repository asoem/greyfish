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

import org.asoem.greyfish.utils.collect.BinaryTree;

/**
 * A k-(d)imensional tree.
 */
public interface KDTree<N extends KDNode<N, ?>> extends BinaryTree<N> {

    /**
     * Get the number of dimensions for the tree nodes
     *
     * @return the number of dimensions
     */
    int dimensions();

    /**
     * Get the size of this tree, which is the node count.
     *
     * @return the size of the tree
     */
    int size();

    /**
     * Search the tree for all nodes which have coordinates that intersect with the hypersphere with given {@code
     * center} and {@code range}.
     *
     * @param center the center of the hypersphere
     * @param range  the radius of the hypersphere
     * @return an iterable of node distance pairs
     */
    Iterable<DistantObject<N>> rangeSearch(double[] center, double range);
}
