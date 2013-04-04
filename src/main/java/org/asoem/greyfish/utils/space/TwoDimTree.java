package org.asoem.greyfish.utils.space;

public interface TwoDimTree<P extends Point2D, T> extends KDTree<P,T> {

    /**
     *
     * @param x the x coordinate of the circle's center
     * @param y the y coordinate of the circle's center
     * @param range the radius of the circle around the point at {@code x} and {@code y}
     * @return all elements whose point intersects with the circle in undefined order
     */
    Iterable<T> findObjects(double x, double y, final double range);
}
