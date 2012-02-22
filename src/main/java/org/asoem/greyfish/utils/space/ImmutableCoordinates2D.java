package org.asoem.greyfish.utils.space;

import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.simpleframework.xml.Attribute;

/**
 * User: christoph
 * Date: 03.03.11
 * Time: 12:07
 */
public class ImmutableCoordinates2D implements Coordinates2D {

    @Attribute(name = "x")
    private final double x;

    @Attribute(name = "y")
    private final double y;

    public ImmutableCoordinates2D(Coordinates2D newCoordinates) {
        this(newCoordinates.getX(), newCoordinates.getY());
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
    private ImmutableCoordinates2D(@Attribute(name = "x") double x,
                                   @Attribute(name = "y") double y) {
        this.x = x;
        this.y = y;
    }

    public static ImmutableCoordinates2D at(double x, double y) {
        return new ImmutableCoordinates2D(x, y);
    }

    public static ImmutableCoordinates2D at(Coordinates2D l2d) {
        return new ImmutableCoordinates2D(l2d.getX(), l2d.getY());
    }

    /**
     * Returns a new ImmutableCoordinates2D with coordinates equal to the sum of each dimension
     * @param l2ds The locations to sum up
     * @return A new ImmutableCoordinates2D
     */
    public static ImmutableCoordinates2D sum(Coordinates2D... l2ds) {
        double xSum = 0;
        double ySum = 0;

        for (Coordinates2D coordinates2D : l2ds) {
            xSum += coordinates2D.getX();
            ySum += coordinates2D.getY();
        }

        return new ImmutableCoordinates2D(xSum, ySum);
    }

    /**
     * Returns a new ImmutableCoordinates2D with coordinates equal to the sum of each dimension
     * @param a The first location
     * @param b The second location
     * @return A new ImmutableCoordinates2D
     */
    public static ImmutableCoordinates2D sum(Coordinates2D a, Coordinates2D b) {
        return at(a.getX() + b.getX(), a.getY() + b.getY());
    }

    public static ImmutableCoordinates2D copyOf(Coordinates2D newCoordinates) {
        return new ImmutableCoordinates2D(newCoordinates);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImmutableCoordinates2D that = (ImmutableCoordinates2D) o;

        return Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0;

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
