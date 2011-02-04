package org.asoem.greyfish.core.space;

public interface MovingObject2DInterface extends Object2DInterface {
	public double getOrientation();
    public void setOrientation(double alpha);
	public double getSpeed();
}
