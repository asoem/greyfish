package org.asoem.greyfish.utils.space;

import com.google.common.base.Function;

public interface TwoDimTree<T> extends Iterable<T> {

    /**
     * Rebuild this {@code KDTree} with the given {@code elements} indexed by {@code Locatable2D}
     * @param elements the objects this {@code KDTree} will contain afterwards
     * @param function the function to compute the {@code Locatable2D} for each element
     */
    void rebuild(final Iterable<? extends T> elements, Function<? super T, ? extends Locatable2D> function);

    /**
     * @param locatable the search point
     * @param range the radius of the circle around {@code locatable}
     * @return evaluates stored objects whose 2D index point intersects with the circle defined by {@code locatable} and {@code range}
     */
    Iterable<T> findObjects(Locatable2D locatable, final double range);
}
