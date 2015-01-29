package org.asoem.greyfish.utils.space;

/**
 * Interface for points in a vector space.
 */
public interface Point extends SpatialObject {

    /**
     * Get the coordinates of the point.
     *
     * @return the coordinates of the point
     */
    double[] coordinates();

    /**
     * Get the coordinate at the given {@code index}.
     *
     * @param index the index index
     * @return the coordinate at given {@code index}
     * @throws java.lang.IndexOutOfBoundsException
     */
    double get(int index);

    /**
     * Calculate the Euclidean distance to given {@code point}.
     *
     * @param point the remote point
     * @return the Euclidean distance to {@code point}
     */
    double distance(Point point);
}
