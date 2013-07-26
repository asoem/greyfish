package org.asoem.greyfish.utils.space;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 12.12.12
 * Time: 12:23
 */
public interface KDTree<P extends Point, T> {
    Iterable<T> findObjects(P center, double range);

    int size();

    @Nullable
    KDNode<P, T> root();
}
