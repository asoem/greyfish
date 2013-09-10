package org.asoem.greyfish.utils.space;

import org.asoem.greyfish.utils.collect.BinaryTree;

import javax.annotation.Nullable;

/**
 * A k-(d)imensional tree.
 */
public interface KDTree<P extends Point, T> extends BinaryTree<KDNode<P, T>> {
    Iterable<T> findObjects(P center, double range);

    int size();

    @Override
    @Nullable
    KDNode<P, T> root();
}
