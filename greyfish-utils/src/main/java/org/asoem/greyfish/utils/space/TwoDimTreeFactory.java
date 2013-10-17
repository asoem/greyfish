package org.asoem.greyfish.utils.space;

import com.google.common.base.Function;

/**
 * User: christoph
 * Date: 05.10.12
 * Time: 18:00
 */
public interface TwoDimTreeFactory<T> {
    TwoDimTree<T> create(Iterable<? extends T> elements, Function<? super T, Point2D> function);
}
