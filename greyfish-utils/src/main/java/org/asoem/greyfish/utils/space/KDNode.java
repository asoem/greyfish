package org.asoem.greyfish.utils.space;

import org.asoem.greyfish.utils.collect.BinaryTreeNode;

/**
 * A node of a {@link KDTree}
 */
public interface KDNode<T, N extends KDNode<T, N>> extends BinaryTreeNode<N, T> {
    int dimensions();

    /**
     * The value stored with this node
     * @return the value stored with this node
     */
    T value();

    /**
     * Calculate the distance of this node to the given {@code coordinates}.
     * The length of {@code coordinates} must match the {@link #dimensions() dimension} of this node.
     *
     * @param coordinates the coordinates to calucalte the distance to
     * @return the distance to a point at given {@code coordinates}
     * @throws IllegalArgumentException if the number of given {@code coordinates}
     * does not match the {@link #dimensions() dimension} of this node
     */
    double distance(double... coordinates);
}
