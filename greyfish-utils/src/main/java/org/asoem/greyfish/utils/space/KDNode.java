package org.asoem.greyfish.utils.space;

import org.asoem.greyfish.utils.collect.TreeNode;

/**
 * User: christoph
 * Date: 12.12.12
 * Time: 14:18
 */
public interface KDNode<P extends Point, T> extends TreeNode<KDNode<P, T>> {
    P point();
    T value();
}
