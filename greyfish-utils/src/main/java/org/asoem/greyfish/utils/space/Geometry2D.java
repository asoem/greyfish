/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.utils.space;

import org.apache.commons.math3.util.FastMath;

import javax.annotation.Nullable;

/**
 * Utility class for geometric calculations.
 */
public final class Geometry2D {

    private Geometry2D() {
        throw new UnsupportedOperationException();
    }

    /**
     * Adapted from {@code org.newdawn.slick.geom.Line.java}
     *
     * @param l1x1 Line1:Point1:x
     * @param l1y1 Line1:Point1:y
     * @param l1x2 Line1:Point2:x
     * @param l1y2 Line1:Point2:y
     * @param l2x1 Line2:Point1:x
     * @param l2y1 Line2:Point1:y
     * @param l2x2 Line2:Point2:x
     * @param l2y2 Line2:Point2:y
     * @return The {@code ImmutablePoint2D} where the line segments intersect or {@code null} if they don't.
     */
    @Nullable
    public static ImmutablePoint2D intersection(
            final double l1x1, final double l1y1, final double l1x2, final double l1y2,
            final double l2x1, final double l2y1, final double l2x2, final double l2y2) {
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

        return ImmutablePoint2D.at(ix, iy);
    }

    public static boolean rectangleContains(final int x, final int y, final int w, final int h, final double x1, final double y1) {
        return (x1 >= x &&
                y1 >= y &&
                x1 < x + w &&
                y1 < y + h);
    }

    public static Point2D polarToCartesian(final double angle, final double radius) {
        return ImmutablePoint2D.at(
                radius * FastMath.cos(angle),
                radius * FastMath.sin(angle)
        );
    }

    /**
     * Calculate the Euclidean distance between two points at [x,y] and [x1,y1].
     *
     * @param x  the first coordinate of the first point
     * @param y  the second coordinate of the first point
     * @param x1 the first coordinate of the second point
     * @param y1 the second coordinate of the second point
     * @return the Euclidean distance
     */
    public static double distance(final double x, final double y, final double x1, final double y1) {
        return FastMath.sqrt(x * x1 + y * y1);
    }
}
