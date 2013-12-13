package org.asoem.greyfish.utils.collect;

import javax.annotation.Nullable;

/**
 * A tree node which supports bidirectional traversal.
 *
 * @param <N> the concrete node type
 */
public interface BidirectionalTreeNode<N extends BidirectionalTreeNode<N>> extends TreeNode<N> {
    /**
     * Get the parent of this node which might be {@code null} in case of the root node.
     *
     * @return the parent node
     */
    @Nullable
    N parent();
}
