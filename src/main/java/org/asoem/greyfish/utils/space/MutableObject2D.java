package org.asoem.greyfish.utils.space;

/**
 * User: christoph
 * Date: 03.03.11
 * Time: 11:29
 */
public class MutableObject2D implements Object2D {

    private MutablePoint2D anchorPoint = new MutablePoint2D();

    public MutableObject2D(double x, double y, double angle) {
        setX(x);
        setY(y);
    }

    @Override
    public Point2D getCentroid() {
        return anchorPoint;
    }

    public static MutableObject2D locatedAt(double x, double y) {
        return new MutableObject2D(x, y, 0);
    }

    public static MutableObject2D at(double x, double y, double angle) {
        return new MutableObject2D(x, y, angle);
    }

    public static MutableObject2D at(Point2D locatable2D, double angle) {
        return new MutableObject2D(locatable2D.getX(), locatable2D.getY(), angle);
    }

    public void setX(double x) {
        anchorPoint.setX(x);
    }

    public void setY(double y) {
        anchorPoint.setY(y);
    }

    @Override
    public int getDimension() {
        return 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MutableObject2D that = (MutableObject2D) o;

        if (!anchorPoint.equals(that.anchorPoint)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return anchorPoint.hashCode();
    }
}
