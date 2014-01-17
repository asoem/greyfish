package org.asoem.greyfish.utils.space;

/**
 * User: christoph Date: 18.10.11 Time: 16:26
 */
public class ImmutableObject2D implements Object2D {

    private final Point2D anchorPoint;

    protected ImmutableObject2D(final Point2D anchorPoint) {
        this.anchorPoint = ImmutablePoint2D.at(anchorPoint);
    }

    @Override
    public int getDimension() {
        return 2;
    }

    @Override
    public Point2D getCentroid() {
        return anchorPoint;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ImmutableObject2D that = (ImmutableObject2D) o;

        if (!anchorPoint.equals(that.anchorPoint)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return anchorPoint.hashCode();
    }

    public static ImmutableObject2D copyOf(final Object2D object2D) {
        return new ImmutableObject2D(object2D.getCentroid());
    }

    public static ImmutableObject2D of(final double x, final double y) {
        return new ImmutableObject2D(ImmutablePoint2D.at(x, y));
    }
}
