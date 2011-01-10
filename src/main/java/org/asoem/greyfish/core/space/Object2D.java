package org.asoem.greyfish.core.space;

import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.ListenerSupport;
import org.simpleframework.xml.Element;

public class Object2D implements Object2DInterface {
	
	private ListenerSupport<Object2DListener> listenerSupport = new ListenerSupport<Object2DListener>();
	
	@Element(name="point")
	protected Location2D anchorPoint;
	
	public Object2D() {
		anchorPoint = new Location2D();
	}
	
	protected Object2D(@Element(name="point") Location2D anchorPoint) {
		this.anchorPoint = anchorPoint;
	}
	
	public Location2D getAnchorPoint() {
		return new Location2D(anchorPoint);
	}

	public void addListener(Object2DListener listener) {
		listenerSupport.addListener(listener);
	}

	public void removeListener(Object2DListener listener) {
		listenerSupport.removeListener(listener);
	}
	
	private void fireHasChanged() {
		listenerSupport.notifyListeners( new Functor<Object2DListener>() {
			
			@Override
			public void update(Object2DListener listener) {
				listener.hasMoved(Object2D.this, null);
			}
		});
	}
	
	@Override
	public void setAnchorPoint(Location2DInterface location2d) {
		setAnchorPoint(location2d.getX(), location2d.getY());
	}
	
	void setAnchorPoint(double x, double y) {
		anchorPoint.set(x, y);
		fireHasChanged();
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
