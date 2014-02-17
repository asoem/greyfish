package org.asoem.greyfish.utils.space;

import com.google.common.base.Function;


public interface TwoDimTreeFactory<T> {
    TwoDimTree<T> create(Iterable<? extends T> elements, Function<? super T, Point2D> function);
}
