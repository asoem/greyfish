package org.asoem.greyfish.utils.space;

/**
 * User: christoph
 * Date: 24.02.12
 * Time: 10:02
 */
public interface SpatialObject {

    /**
     * Get the dimension of the object coordinates
     * @return the dimension of the coordinates defining this object
     */
    int getDimension();

    /**
     * Get the anchor point for the bounding volume of this object
     * @return the anchor point for the bounding volume
     */
    Point getCentroid();
}
