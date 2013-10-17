package org.asoem.greyfish.utils.collect;

import javax.annotation.Nullable;

public interface BidirectionalTreeNode<E, N extends BidirectionalTreeNode<E, N>> extends TreeNode<N, E> {
    @Nullable
    N parent();
}
