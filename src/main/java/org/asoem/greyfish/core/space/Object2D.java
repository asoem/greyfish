package org.asoem.greyfish.core.space;

/**
 * User: christoph
 * Date: 03.03.11
 * Time: 11:18
 */
public interface Object2D extends Location2D {
    public Location2D getAnchorPoint();
    public void setAnchorPoint(Location2D location2d);

    public double getOrientation();
    public void setOrientation(double alpha);
}
