package org.asoem.greyfish.core.space;

import org.asoem.greyfish.utils.PolarPoint;

public class MovingObject2D extends Object2D implements MovingObject2DInterface {

	private final PolarPoint polarPoint = new PolarPoint();
	
	public MovingObject2D() {
	}
	
	@Override
	public double getOrientation() {
		return polarPoint.getPhi();
	}
	
	@Override
	public double getSpeed() {
		return polarPoint.getDistance();
	}
	
	public void setSpeed(float speed) {
		polarPoint.setDistance(speed);
	}
	
	@Override
	public void setOrientation(double alpha) {
		polarPoint.addToPhi(alpha);
	}
}
