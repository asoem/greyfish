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
 * An interface for {@link KDTree kd-trees} with two dimensions.
 *
 * @param <T>
 */
public interface TwoDimTree<T> extends KDTree<TwoDimTree.Node<T>> {

    /**
     * @param x     the x coordinate of the circle's center
     * @param y     the y coordinate of the circle's center
     * @param range the radius of the circle around the point at {@code x} and {@code y}
     * @return all nodes whose point intersects with the circle in undefined order
     * @deprecated Use {@link #rangeSearch}.
     */
    @Deprecated
    Iterable<DistantObject<TwoDimTree.Node<T>>> findNodes(double x, double y, final double range);

    /**
     * Search the tree for all nodes which have coordinates that intersect with the circle having its center at {@code
     * x,y} and given {@code radius}.
     *
     * @param x      the x coordinate of the circle's center
     * @param y      the y coordinate of the circle's center
     * @param radius the radius of the circle around the point at {@code x} and {@code y}
     * @return All nodes in the tree whose coordinates intersect with the circle
     */
    Iterable<DistantObject<TwoDimTree.Node<T>>> rangeSearch(double x, double y, final double radius);

    /**
     * A node of a {@code TwoDimTree}
     *
     * @param <T> the type of the object a node holds
     */
    interface Node<T> extends KDNode<Node<T>, T> {

        /**
         * The x coordinate of the node
         * @return The x coordinate of the node
         */
        double xCoordinate();

        /**
         * The y coordinate of the node
         * @return The y coordinate of the node
         */
        double yCoordinate();
    }
}
