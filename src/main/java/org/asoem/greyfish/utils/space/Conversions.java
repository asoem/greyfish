package org.asoem.greyfish.utils.space;

import org.asoem.greyfish.utils.math.PolarPoint2D;

/**
 * User: christoph
 * Date: 20.10.11
 * Time: 15:10
 */
public class Conversions {

    public static Coordinates2D toCartesian(PolarPoint2D polarPoint2D) {
        return ImmutableCoordinates2D.at(
                polarPoint2D.getDistance() * Math.cos(polarPoint2D.getAngle()),
                polarPoint2D.getDistance() * Math.sin(polarPoint2D.getAngle())
        );
    }
}
