package org.asoem.greyfish.utils.collect;

import com.google.common.base.Optional;

/**
 * A node of a {@link BinaryTree}
 */
public interface BinaryTreeNode<N extends BinaryTreeNode<N>> extends TreeNode<N> {

    /**
     * Get the left child of the node.
     *
     * @return the left child
     */
    Optional<N> leftChild();

    /**
     * Get the right child of the node.
     *
     * @return the right child
     */
    Optional<N> rightChild();
}
