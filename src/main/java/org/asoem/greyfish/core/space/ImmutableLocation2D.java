package org.asoem.greyfish.core.space;

/**
 * User: christoph
 * Date: 03.03.11
 * Time: 12:07
 */
public class ImmutableLocation2D implements Location2D {

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

    private ImmutableLocation2D(double x, double y) {
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
     * @param a The first location
     * @param b The second location
     * @return A new ImmutableLocation2D
     */
    public static ImmutableLocation2D at(Location2D a, Location2D b) {
        return new ImmutableLocation2D(a.getX() + b.getX(), a.getY() + b.getY());
    }

    /**
     * Returns a new ImmutableLocation2D with coordinates equal to the sum of each dimension
     * @param l2ds The locations to sum up
     * @return A new ImmutableLocation2D
     */
    public static ImmutableLocation2D at(Location2D ... l2ds) {
        double xSum = 0;
        double ySum = 0;

        for (Location2D location2D : l2ds) {
            xSum += location2D.getX();
            ySum += location2D.getY();
        }

        return new ImmutableLocation2D(xSum, ySum);
    }
}
