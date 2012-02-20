package org.asoem.greyfish.utils.space;

import com.google.common.base.Function;

public interface TwoDimTree<T> extends Iterable<T> {

    /**
     * Rebuild this {@code KDTree} with the given {@code elements} indexed by {@code Coordinates2D}
     * @param elements the objects this {@code KDTree} will contain afterwards
     * @param function the function to compute the {@code Coordinates2D} for each element
     */
    void rebuild(final Iterable<? extends T> elements, Function<? super T, Coordinates2D> function);

    /**
     * @param coordinates the search point
     * @param range the radius of the circle around {@code coordinates}
     * @return evaluates stored objects whose 2D index point intersects with the circle defined by {@code coordinates} and {@code range}
     */
    Iterable<T> findObjects(Coordinates2D coordinates, final double range);
}
