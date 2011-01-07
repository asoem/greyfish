package org.asoem.greyfish.core.space;


public interface Object2DInterface extends Location2DInterface {
	public Location2D getAnchorPoint();
	public void addListener(Object2DListener listener);
	public void removeListener(Object2DListener listener);
	public void setAnchorPoint(Location2DInterface location2d);
}
