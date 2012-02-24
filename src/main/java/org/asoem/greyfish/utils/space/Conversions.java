package org.asoem.greyfish.utils.space;

import org.asoem.greyfish.utils.math.PolarPoint2D;

/**
 * User: christoph
 * Date: 20.10.11
 * Time: 15:10
 */
public class Conversions {

    public static Location2D polarToCartesian(PolarPoint2D polarPoint2D) {
        return polarToCartesian(polarPoint2D.getAngle(), polarPoint2D.getRadius());
    }

    public static Location2D polarToCartesian(double angle, double radius) {
        return ImmutableLocation2D.at(
                radius * Math.cos(angle),
                radius * Math.sin(angle)
        );
    }
}
