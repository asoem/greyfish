package org.asoem.greyfish.utils.collect;

/**
 * User: christoph
 * Date: 05.10.11
 * Time: 09:04
 */
public interface TreeNode<T extends TreeNode<T>> {
    Iterable<T> childConditions();
}
