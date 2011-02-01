package org.asoem.greyfish.core.space;


public interface Object2DInterface extends Location2DInterface {
	public Location2DInterface getAnchorPoint();
	public void setAnchorPoint(Location2DInterface location2d);

	public void addListener(Object2DListener listener);
	public void removeListener(Object2DListener listener);
}
