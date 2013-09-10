package org.asoem.greyfish.utils.collect;

/**
 * A node of a {@link Tree}
 */
public interface TreeNode<T extends TreeNode<T>> {
    Iterable<T> children();
}
