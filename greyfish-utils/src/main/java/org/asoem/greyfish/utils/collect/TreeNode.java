package org.asoem.greyfish.utils.collect;

/**
 * A node of a {@link Tree}
 */
public interface TreeNode<N extends TreeNode<N, T>, T> {
    Iterable<N> children();
}
