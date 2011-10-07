package org.asoem.greyfish.core.space;

/**
 * User: christoph
 * Date: 03.03.11
 * Time: 12:07
 */
public class ImmutableCoordinates2D implements Coordinates2D {

    private final double x;
    private final double y;


    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    private ImmutableCoordinates2D(double x, double y) {
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
     * @param a The first location
     * @param b The second location
     * @return A new ImmutableCoordinates2D
     */
    public static ImmutableCoordinates2D at(Coordinates2D a, Coordinates2D b) {
        return new ImmutableCoordinates2D(a.getX() + b.getX(), a.getY() + b.getY());
    }

    /**
     * Returns a new ImmutableCoordinates2D with coordinates equal to the sum of each dimension
     * @param l2ds The locations to sum up
     * @return A new ImmutableCoordinates2D
     */
    public static ImmutableCoordinates2D at(Coordinates2D... l2ds) {
        double xSum = 0;
        double ySum = 0;

        for (Coordinates2D coordinates2D : l2ds) {
            xSum += coordinates2D.getX();
            ySum += coordinates2D.getY();
        }

        return new ImmutableCoordinates2D(xSum, ySum);
    }

    public static Coordinates2D sum(Coordinates2D a, Coordinates2D b) {
        return at(a.getX() + b.getX(), a.getY() + b.getY());
    }
}
