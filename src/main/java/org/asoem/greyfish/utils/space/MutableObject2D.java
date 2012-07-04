package org.asoem.greyfish.utils.space;

import javolution.lang.MathLib;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 03.03.11
 * Time: 11:29
 */
public class MutableObject2D implements Object2D {

    @Attribute(name = "orientation")
    private double orientation = 0;

    @Element(name = "anchorPoint")
    private MutablePoint2D anchorPoint = new MutablePoint2D();

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private MutableObject2D() {}

    public MutableObject2D(double x, double y, double angle) {
        setX(x);
        setY(y);
        setOrientation(angle);
    }

    public void setCoordinates(Point2D locatable2d) {
        checkNotNull(locatable2d);
        setX(locatable2d.getX());
        setY(locatable2d.getY());
    }

    @Override
    public double getOrientationAngle() {
        return orientation;
    }

    @Override
    public Point2D getAnchorPoint() {
        return anchorPoint;
    }

    public void setOrientation(double alpha) {
        checkArgument(alpha >= 0 && alpha <= MathLib.TWO_PI, "Given angle is out of range [0, TWO_PI]");
        this.orientation = alpha;
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
    public int getDimensions() {
        return 2;
    }

    @Override
    public double[] getOrientation() {
        return new double[] {orientation};
    }

    @Override
    public double[] getBoundingVolume() {
        return new double[] {0, 0};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MutableObject2D)) return false;

        MutableObject2D that = (MutableObject2D) o;

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
}
