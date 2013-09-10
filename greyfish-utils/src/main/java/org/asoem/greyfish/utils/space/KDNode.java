package org.asoem.greyfish.utils.space;

import org.asoem.greyfish.utils.collect.BinaryTreeNode;

/**
 * A node of a {@link KDTree}
 */
public interface KDNode<P extends Point, T> extends BinaryTreeNode<KDNode<P, T>> {
    /**
     * The k-dimensional point for this node
     * @return the point for this node
     */
    P point();

    /**
     * The value stored with this node
     * @return the value stored with this node
     */
    T value();
}
