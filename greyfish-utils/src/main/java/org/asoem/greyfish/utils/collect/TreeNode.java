package org.asoem.greyfish.utils.collect;

/**
 * A node of a {@link Tree}
 */
public interface TreeNode<N extends TreeNode<N>> {
    /**
     * Get all children of this node.
     *
     * @return all child nodes
     */
    Iterable<N> children();
}
