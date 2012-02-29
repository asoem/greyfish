package org.asoem.greyfish.utils.space;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 29.02.12
 * Time: 10:23
 */
public class Geometry2D {

    /**
     * Adapted from {@code org.newdawn.slick.geom.Line.java}
     * @param l1x1 Line1:Point1:x
     * @param l1y1 Line1:Point1:y
     * @param l1x2 Line1:Point2:x
     * @param l1y2 Line1:Point2:y
     * @param l2x1 Line2:Point1:x
     * @param l2y1 Line2:Point1:y
     * @param l2x2 Line2:Point2:x
     * @param l2y2 Line2:Point2:y
     * @return The {@code ImmutableLocation2D} where the line segments intersect or {@code null} if they don't.
     */
    @Nullable
    public static ImmutableLocation2D intersection(double l1x1, double l1y1, double l1x2, double l1y2,
                                                   double l2x1, double l2y1, double l2x2, double l2y2) {
        final double dx1 = l1x2 - l1x1;
        final double dx2 = l2x2 - l2x1;
        final double dy1 = l1y2 - l1y1;
        final double dy2 = l2y2 - l2y1;
        final double denom = (dy2 * dx1) - (dx2 * dy1);

        if (denom == 0) {
            return null;
        }

        final double ua = ((dx2 * (l1y1 - l2y1)) - (dy2 * (l1x1 - l2x1))) / denom;
        final double ub = ((dx1 * (l1y1 - l2y1)) - (dy1 * (l1x1 - l2x1))) / denom;

        if (((ua < 0) || (ua > 1) || (ub < 0) || (ub > 1))) {
            return null;
        }

        final double ix = l1x1 + (ua * (l1x2 - l1x1));
        final double iy = l1y1 + (ua * (l1y2 - l1y1));

        return ImmutableLocation2D.at(ix, iy);
    }
}
