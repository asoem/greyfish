package org.asoem.greyfish.utils.space;

import org.simpleframework.xml.Attribute;

/**
 * User: christoph
 * Date: 24.02.12
 * Time: 10:05
 */
public class MutableLocation2D implements Location2D {

    @Attribute
    private double x;

    @Attribute
    private double y;

    public MutableLocation2D() {
    }

    public MutableLocation2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public double getX() {
        return x;
    }

    public void setX(double x) {
       this.x = x;
    }

    @Override
    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public double[] getCoordinates() {
        return new double[] {x, y};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MutableLocation2D)) return false;

        MutableLocation2D that = (MutableLocation2D) o;

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
}
