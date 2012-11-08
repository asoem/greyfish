package org.asoem.greyfish.utils.space;

import org.apache.commons.math3.util.MathUtils;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: christoph
 * Date: 18.10.11
 * Time: 16:26
 */
public class ImmutableObject2D implements Object2D {

    @Attribute(name = "orientation")
    private final double orientation;

    @Element(name = "anchorPoint")
    private final Point2D anchorPoint;

    protected ImmutableObject2D(@Element(name = "anchorPoint") Point2D anchorPoint,
                                @Attribute(name = "orientation") double orientation) {
        checkArgument(orientation >= 0 && orientation < MathUtils.TWO_PI, "Given angle is out of range [0, TWO_PI): %s", orientation);
        this.orientation = orientation;
        this.anchorPoint = ImmutablePoint2D.at(anchorPoint);
    }

    @Override
    public double getOrientationAngle() {
        return orientation;
    }

    @Override
    public int getDimensions() {
        return 2;
    }

    @Override
    public double[] getOrientation() {
        return new double[] {orientation};
    }

    @Override
    public double[] getBoundingVolume() {
        return new double[] {0.1f, 0.1f}; // todo: implement
    }

    @Override
    public Point2D getAnchorPoint() {
        return anchorPoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableObject2D)) return false;

        ImmutableObject2D that = (ImmutableObject2D) o;

        if (Double.compare(that.orientation, orientation) != 0) return false;
        if (!anchorPoint.equals(that.anchorPoint)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = orientation != +0.0d ? Double.doubleToLongBits(orientation) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + anchorPoint.hashCode();
        return result;
    }

    public static ImmutableObject2D copyOf(Object2D object2D) {
        return new ImmutableObject2D(object2D.getAnchorPoint(), object2D.getOrientationAngle());
    }

    public static ImmutableObject2D of(double x, double y, double orientationAngle) {
        return new ImmutableObject2D(ImmutablePoint2D.at(x, y), orientationAngle);
    }

    public static ImmutableObject2D rotated(Object2D object2D, double rotation) {
        return new ImmutableObject2D(object2D.getAnchorPoint(), rotation);
    }
}
