package org.asoem.greyfish.core.space;

public interface MovingObject2DInterface extends Object2DInterface {
	public double getOrientation();
	public double getSpeed();
	public void rotate(double alpha);
}
