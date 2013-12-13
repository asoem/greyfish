package org.asoem.greyfish.utils.collect;

import javax.annotation.Nullable;

/**
 * A node of a {@link BinaryTree}
 */
public interface BinaryTreeNode<N extends BinaryTreeNode<N>> extends TreeNode<N> {
    /**
     * Get the left child of the node.
     *
     * @return the left child, which might be {@code null}
     */
    @Nullable
    N leftChild();

    /**
     * Get the right child of the node.
     *
     * @return the right child, which might be {@code null}
     */
    @Nullable
    N rightChild();
}
