package org.asoem.greyfish.utils.collect;

import javax.annotation.Nullable;

/**
 * A node of a {@link BinaryTree}
 */
public interface BinaryTreeNode<T extends BinaryTreeNode<T>> extends TreeNode<T> {
    /**
     * Get the left child of the node.
     * @return the left child, which might be {@code null}
     */
    @Nullable
    T leftChild();

    /**
     * Get the right child of the node.
     * @return the right child, which might be {@code null}
     */
    @Nullable
    T rightChild();
}
