package org.asoem.greyfish.utils.space;

import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.simpleframework.xml.Attribute;

import java.util.Arrays;

/**
 * User: christoph
 * Date: 03.03.11
 * Time: 12:07
 */
public class ImmutableLocatable2D implements Locatable2D {

    private final double[] coordinates;
    
    public ImmutableLocatable2D(Locatable2D newLocatable) {
        this(newLocatable.getX(), newLocatable.getY());
    }

    @Attribute(name = "x")
    @Override
    public double getX() {
        return coordinates[0];
    }

    @Attribute(name = "y")
    @Override
    public double getY() {
        return coordinates[1];
    }

    @SimpleXMLConstructor
    private ImmutableLocatable2D(@Attribute(name = "x") double x,
                                 @Attribute(name = "y") double y) {
        this.coordinates = new double[] {x, y};
    }

    public static ImmutableLocatable2D at(double x, double y) {
        return new ImmutableLocatable2D(x, y);
    }

    public static ImmutableLocatable2D at(Locatable2D l2d) {
        return new ImmutableLocatable2D(l2d.getX(), l2d.getY());
    }

    /**
     * Returns a new ImmutableLocatable2D with coordinates equal to the sum of each dimension
     * @param l2ds The locations to sum up
     * @return A new ImmutableLocatable2D
     */
    public static ImmutableLocatable2D sum(Locatable2D... l2ds) {
        double xSum = 0;
        double ySum = 0;

        for (Locatable2D locatable2D : l2ds) {
            xSum += locatable2D.getX();
            ySum += locatable2D.getY();
        }

        return new ImmutableLocatable2D(xSum, ySum);
    }

    /**
     * Returns a new ImmutableLocatable2D with coordinates equal to the sum of each dimension
     * @param a The first location
     * @param b The second location
     * @return A new ImmutableLocatable2D
     */
    public static ImmutableLocatable2D sum(Locatable2D a, Locatable2D b) {
        return at(a.getX() + b.getX(), a.getY() + b.getY());
    }

    public static ImmutableLocatable2D copyOf(Locatable2D newLocatable) {
        return new ImmutableLocatable2D(newLocatable);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableLocatable2D)) return false;

        ImmutableLocatable2D that = (ImmutableLocatable2D) o;

        return Arrays.equals(coordinates, that.coordinates);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(coordinates);
    }

    @Override
    public double[] getCoordinates() {
        return coordinates;
    }
}
