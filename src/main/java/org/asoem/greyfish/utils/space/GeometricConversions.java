package org.asoem.greyfish.utils.space;

import org.apache.commons.math3.util.FastMath;

/**
 * User: christoph
 * Date: 20.10.11
 * Time: 15:10
 */
public final class GeometricConversions {

    private GeometricConversions() {}

    public static Point2D polarToCartesian(double angle, double radius) {
        return ImmutablePoint2D.at(
                radius * FastMath.cos(angle),
                radius * FastMath.sin(angle)
        );
    }
}
