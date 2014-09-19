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
}
