package org.asoem.greyfish.utils.collect;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 22.03.12
 * Time: 15:10
 */
public interface BidirectionalTreeNode<T extends BidirectionalTreeNode<T>> extends TreeNode<T> {
    @Nullable
    T parent();
}
