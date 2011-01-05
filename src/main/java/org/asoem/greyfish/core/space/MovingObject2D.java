package org.asoem.sico.core.space;

import org.asoem.sico.utils.PolarPoint;

public class MovingObject2D extends Object2D implements MovingObject2DInterface {

	private final PolarPoint polarPoint = new PolarPoint();
	
	public MovingObject2D() {
	}
	
	@Override
	public float getOrientation() {
		return polarPoint.getPhi();
	}
	
	@Override
	public float getSpeed() {
		return polarPoint.getDistance();
	}
	
	public void setSpeed(float speed) {
		polarPoint.setDistance(speed);
	}
	
	@Override
	public void rotate(float alpha) {
		polarPoint.addToPhi(alpha);
	}
}
