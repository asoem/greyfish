package org.asoem.greyfish.utils.space;

/**
 * User: christoph
 * Date: 03.03.11
 * Time: 12:07
 */
public class ImmutablePoint2D extends AbstractPoint2D {

    private final double x;

    private final double y;
    
    public ImmutablePoint2D(final Point2D newLocatable) {
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

    private ImmutablePoint2D(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    public static ImmutablePoint2D at(final double x, final double y) {
        return new ImmutablePoint2D(x, y);
    }

    public static ImmutablePoint2D at(final Point2D l2d) {
        return new ImmutablePoint2D(l2d.getX(), l2d.getY());
    }

    /**
     * Returns a new ImmutablePoint2D with coordinates equal to the sum of each dimension
     * @param l2ds The locations to sum up
     * @return A new ImmutablePoint2D
     */
    public static ImmutablePoint2D sum(final Point2D... l2ds) {
        double xSum = 0;
        double ySum = 0;

        for (final Point2D locatable2D : l2ds) {
            xSum += locatable2D.getX();
            ySum += locatable2D.getY();
        }

        return new ImmutablePoint2D(xSum, ySum);
    }

    /**
     * Returns a new ImmutablePoint2D with coordinates equal to the sum of each dimension
     * @param a The first location
     * @param b The second location
     * @return A new ImmutablePoint2D
     */
    public static ImmutablePoint2D sum(final Point2D a, final Point2D b) {
        return at(a.getX() + b.getX(), a.getY() + b.getY());
    }

    public static ImmutablePoint2D copyOf(final Point2D newLocatable) {
        return new ImmutablePoint2D(newLocatable);
    }

    @Override
    public String toString() {
        return "ImmutablePoint2D{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutablePoint2D)) return false;

        final ImmutablePoint2D that = (ImmutablePoint2D) o;

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

    public ImmutablePoint2D add(final double i, final double v) {
        return ImmutablePoint2D.at(x + i, y + v);
    }

    public ImmutablePoint2D subtract(final double xo, final double yo) {
        return new ImmutablePoint2D(x - xo, y - yo);
    }

    public ImmutablePoint2D scale(final double v) {
        return new ImmutablePoint2D(x * v, y * v);
    }

    public ImmutablePoint2D add(final ImmutablePoint2D scale) {
        return add(scale.getX(), scale.getY());
    }
}
