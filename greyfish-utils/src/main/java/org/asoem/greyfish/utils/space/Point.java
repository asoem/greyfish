package org.asoem.greyfish.utils.space;

/**
 * A point in space.
 */
public interface Point extends SpatialObject {
    double[] getCoordinate();

    /**
     * Calculate the Euclidean distance to given {@code point}.
     *
     * @param point the remote point
     * @return the Euclidean distance to {@code point}
     */
    double distance(Point point);
}
