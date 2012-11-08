package org.asoem.greyfish.utils.space;

/**
 * User: christoph
 * Date: 24.02.12
 * Time: 10:02
 */
public interface SpatialObject {

    /**
     * Get the number of dimensions for this object
     * @return the number of dimensions for this object
     */
    int getDimensions();

    /**
     * Get the orientation of this object
     * @return the orientation of this object
     */
    double[] getOrientation();

    /**
     * Get the minimal volume completely enclosing this object
     * @return the minimal volume completely enclosing this object
     */
    double[] getBoundingVolume();

    /**
     * Get the anchor point for the bounding volume of this object
     * @return the anchor point for the bounding volume
     */
    Point getAnchorPoint();
}
