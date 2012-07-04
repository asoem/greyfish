package org.asoem.greyfish.utils.space;

import com.google.common.base.Function;
import org.asoem.greyfish.utils.base.Product2;

public interface TwoDimTree<T> {

    /**
     * Rebuild this {@code KDTree} with the given {@code elements} indexed by {@code Point2D}
     * @param elements the objects this {@code KDTree} will contain afterwards
     * @param function the function to compute the {@code Point2D} for each element
     */
    void rebuild(final Iterable<? extends T> elements, Function<? super T, ? extends Product2<Double,Double>> function);

    /**
     *
     * @param x
     * @param y
     *@param range the radius of the circle around {@code locatable}  @return all elements whose 2D index point intersects with the circle defined by {@code locatable} and {@code range}
     * in undefined order
     */
    Iterable<T> findObjects(double x, double y, final double range);
}
