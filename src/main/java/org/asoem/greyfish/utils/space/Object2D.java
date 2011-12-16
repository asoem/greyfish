package org.asoem.greyfish.utils.space;

/**
 * User: christoph
 * Date: 03.03.11
 * Time: 11:18
 */
public interface Object2D extends Location2D {
    /**
     * @return the orientation (angle difference from 0) of this object2D
     */
    public double getOrientation();

    /**
     * @return the coordinates of the anchorpoint of this object2D
     */
    @Override
    Coordinates2D getCoordinates();
}
