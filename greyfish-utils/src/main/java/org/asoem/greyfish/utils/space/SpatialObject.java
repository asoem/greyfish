package org.asoem.greyfish.utils.space;


public interface SpatialObject {

    /**
     * Get the dimension of the object coordinates.
     *
     * @return the dimension of the coordinates defining this object
     */
    int getDimension();

    /**
     * Get the centroid for this shape.
     *
     * <p>A centroid is defined as the mean position of all the points in all of the coordinate directions.</p>
     *
     * @return the centroid for this shape
     */
    Point getCentroid();

}
