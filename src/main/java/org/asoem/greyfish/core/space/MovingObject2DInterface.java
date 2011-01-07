package org.asoem.greyfish.core.space;

public interface MovingObject2DInterface extends Object2DInterface {
	public float getOrientation();	
	public float getSpeed();	
	public void rotate(float alpha);
}
