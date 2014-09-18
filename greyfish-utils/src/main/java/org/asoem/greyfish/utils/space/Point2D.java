package org.asoem.greyfish.utils.space;

import org.asoem.greyfish.utils.collect.Product2;

/**
 * A two dimensional point.
 */
public interface Point2D extends Point, Object2D, Product2<Double, Double> {
    /**
     * Get the x coordinate.
     *
     * @return the x coordinate
     */
    double getX();

    /**
     * Get the y coordinate.
     *
     * @return the y coordinate
     */
    double getY();
}
