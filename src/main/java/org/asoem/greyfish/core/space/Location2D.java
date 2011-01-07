package org.asoem.greyfish.core.space;

import java.awt.geom.Point2D;

import org.simpleframework.xml.Attribute;

import com.google.common.primitives.Floats;

public class Location2D implements Location2DInterface {

	@Attribute
	private float x;
	@Attribute
	private float y;

	public Location2D() {
	}

	public Location2D(Location2D location2d) {
		set(location2d);
	}

	public Location2D(float x, float y) {
		set(x, y);
	}
	
	public void set(Location2D location2d) {
		set(location2d.x, location2d.y);
	}

	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return Floats.join(",", x, y);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location2D other = (Location2D) obj;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		return true;
	}
	
	public Point2D asPoint2D() {
		return new Point2D.Float(x, y);
	}
}
