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
