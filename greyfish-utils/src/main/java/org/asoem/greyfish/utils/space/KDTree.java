package org.asoem.greyfish.utils.space;

import org.asoem.greyfish.utils.collect.BinaryTree;

/**
 * A k-(d)imensional tree.
 */
public interface KDTree<N extends KDNode<?, N>> extends BinaryTree<N> {
    int dimensions();
    int size();
}
