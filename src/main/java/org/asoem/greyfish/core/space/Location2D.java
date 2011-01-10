package org.asoem.greyfish.core.space;

import com.google.common.primitives.Doubles;
import org.simpleframework.xml.Attribute;

import java.awt.geom.Point2D;

public class Location2D implements Location2DInterface {

	@Attribute
	private double x;
	@Attribute
	private double y;

	public Location2D() {
	}

	public Location2D(Location2D location2d) {
		set(location2d);
	}

	public Location2D(double x, double y) {
		set(x, y);
	}
	
	public void set(Location2D location2d) {
		set(location2d.x, location2d.y);
	}

	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return Doubles.join(",", x, y);
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location2D that = (Location2D) o;

        if (Double.compare(that.x, x) != 0) return false;
        if (Double.compare(that.y, y) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = x != +0.0d ? Double.doubleToLongBits(x) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = y != +0.0d ? Double.doubleToLongBits(y) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public Point2D asPoint2D() {
		return new Point2D.Double(x, y);
	}
}
