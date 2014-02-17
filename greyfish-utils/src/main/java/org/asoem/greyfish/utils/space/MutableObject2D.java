package org.asoem.greyfish.utils.space;


public class MutableObject2D implements Object2D {

    private MutablePoint2D anchorPoint = new MutablePoint2D();

    public MutableObject2D(final double x, final double y, final double angle) {
        setX(x);
        setY(y);
    }

    @Override
    public Point2D getCentroid() {
        return anchorPoint;
    }

    public static MutableObject2D locatedAt(final double x, final double y) {
        return new MutableObject2D(x, y, 0);
    }

    public static MutableObject2D at(final double x, final double y, final double angle) {
        return new MutableObject2D(x, y, angle);
    }

    public static MutableObject2D at(final Point2D locatable2D, final double angle) {
        return new MutableObject2D(locatable2D.getX(), locatable2D.getY(), angle);
    }

    public void setX(final double x) {
        anchorPoint.setX(x);
    }

    public void setY(final double y) {
        anchorPoint.setY(y);
    }

    @Override
    public int getDimension() {
        return 2;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final MutableObject2D that = (MutableObject2D) o;

        if (!anchorPoint.equals(that.anchorPoint)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return anchorPoint.hashCode();
    }
}
