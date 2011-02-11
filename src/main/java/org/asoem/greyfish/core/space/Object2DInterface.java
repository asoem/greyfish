package org.asoem.greyfish.core.space;


public interface Object2DInterface extends Location2DInterface {
    public Location2DInterface getAnchorPoint();
    public void setAnchorPoint(Location2DInterface location2d);

    public double getOrientation();
    public void setOrientation(double alpha);
}
