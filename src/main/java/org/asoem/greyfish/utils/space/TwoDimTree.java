package org.asoem.greyfish.utils.space;

public interface TwoDimTree<T> {

    /**
     *
     * @param x
     * @param y
     *@param range the radius of the circle around {@code locatable}  @return all elements whose 2D index point intersects with the circle defined by {@code locatable} and {@code range}
     * in undefined order
     */
    Iterable<T> findObjects(double x, double y, final double range);
}
