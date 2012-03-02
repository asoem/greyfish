package org.asoem.greyfish.utils.space;

import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.utils.base.Builder;
import org.simpleframework.xml.Attribute;

/**
 * User: christoph
 * Date: 03.03.11
 * Time: 12:07
 */
public class ImmutableLocation2D implements Location2D {

    @Attribute(name = "x")
    private final double x;

    @Attribute(name = "y")
    private final double y;
    
    public ImmutableLocation2D(Location2D newLocatable) {
        this(newLocatable.getX(), newLocatable.getY());
    }
    
    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @SimpleXMLConstructor
    private ImmutableLocation2D(@Attribute(name = "x") double x,
                                @Attribute(name = "y") double y) {
        this.x = x;
        this.y = y;
    }

    public static ImmutableLocation2D at(double x, double y) {
        return new ImmutableLocation2D(x, y);
    }

    public static ImmutableLocation2D at(Location2D l2d) {
        return new ImmutableLocation2D(l2d.getX(), l2d.getY());
    }

    /**
     * Returns a new ImmutableLocation2D with coordinates equal to the sum of each dimension
     * @param l2ds The locations to sum up
     * @return A new ImmutableLocation2D
     */
    public static ImmutableLocation2D sum(Location2D... l2ds) {
        double xSum = 0;
        double ySum = 0;

        for (Location2D locatable2D : l2ds) {
            xSum += locatable2D.getX();
            ySum += locatable2D.getY();
        }

        return new ImmutableLocation2D(xSum, ySum);
    }

    /**
     * Returns a new ImmutableLocation2D with coordinates equal to the sum of each dimension
     * @param a The first location
     * @param b The second location
     * @return A new ImmutableLocation2D
     */
    public static ImmutableLocation2D sum(Location2D a, Location2D b) {
        return at(a.getX() + b.getX(), a.getY() + b.getY());
    }

    public static ImmutableLocation2D copyOf(Location2D newLocatable) {
        return new ImmutableLocation2D(newLocatable);
    }

    @Override
    public double[] getCoordinates() {
        return new double[] {x, y};
    }

    @Override
    public String toString() {
        return "ImmutableLocation2D{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableLocation2D)) return false;

        ImmutableLocation2D that = (ImmutableLocation2D) o;

        if (that.x != x) return false;
        if (that.y != y) return false;

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

    public ImmutableLocation2D add(double i, double v) {
        return ImmutableLocation2D.at(x + i, y + v);
    }

    public ImmutableLocation2D subtract(double xo, double yo) {
        return new ImmutableLocation2D(x - xo, y - yo);
    }

    public ImmutableLocation2D scale(double v) {
        return new ImmutableLocation2D(x * v, y * v);
    }

    public ImmutableLocation2D add(ImmutableLocation2D scale) {
        return add(scale.getX(), scale.getY());
    }
}
