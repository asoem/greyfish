package org.asoem.greyfish.utils.space;

import com.google.common.base.Optional;
import com.google.common.collect.BinaryTreeTraverser;

public final class TwoDimTreeTraverser<T> extends BinaryTreeTraverser<TwoDimTree.Node<T>> {
    @Override
    public Optional<TwoDimTree.Node<T>> leftChild(final TwoDimTree.Node<T> root) {
        return root.leftChild();
    }

    @Override
    public Optional<TwoDimTree.Node<T>> rightChild(final TwoDimTree.Node<T> root) {
        return root.rightChild();
    }
}
