package org.asoem.greyfish.core.space;

import org.simpleframework.xml.Element;

public class Object2D implements Object2DInterface {
	
	@Element(name="point")
	protected final Location2D anchorPoint;
	
	public Object2D() {
		anchorPoint = new Location2D();
	}
	
	protected Object2D(@Element(name="point") Location2D anchorPoint) {
		this.anchorPoint = anchorPoint;
	}
	
	public Location2DInterface getAnchorPoint() {
		return new Location2D(anchorPoint);
	}
	
	@Override
	public void setAnchorPoint(Location2DInterface location2d) {
		setAnchorPoint(location2d.getX(), location2d.getY());
	}
	
	void setAnchorPoint(double x, double y) {
		anchorPoint.set(x, y);
	}

	@Override
	public double getX() {
		return anchorPoint.getX();
	}

	@Override
	public double getY() {
		return anchorPoint.getY();
	}
}
