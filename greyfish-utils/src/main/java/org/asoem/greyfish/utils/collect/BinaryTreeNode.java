package org.asoem.greyfish.utils.collect;

/**
 * A node of a {@link BinaryTree}
 */
public interface BinaryTreeNode<T extends BinaryTreeNode<T>> extends TreeNode<T> {
    T leftChild();
    T rightChild();
}
