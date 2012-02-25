package org.asoem.greyfish.utils.space;

import javolution.lang.MathLib;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 18.10.11
 * Time: 16:26
 */
public class ImmutableObject2D implements Object2D {

    @Element(name = "orientation")
    private final double orientation;

    @Element(name = "locatable")
    private final Location2D locatable;

    @SimpleXMLConstructor
    private ImmutableObject2D(@Element(name = "locatable") Location2D locatable,
                              @Element(name = "orientation") double orientation) {
        checkNotNull(locatable);
        checkArgument(orientation >= 0 && orientation <= MathLib.TWO_PI, "Given angle is out of range [0, TWO_PI]: " + orientation);
        this.locatable = locatable;
        this.orientation = orientation;
    }

    @Override
    public double getOrientationAngle() {
        return orientation;
    }

    public static ImmutableObject2D of(Location2D locatable, double orientationAngle) {
        checkNotNull(locatable);
        return new ImmutableObject2D(locatable, orientationAngle);
    }

    @Override
    public double getX() {
        return locatable.getX();
    }

    @Override
    public double getY() {
        return locatable.getY();
    }

    @Override
    public double[] getCoordinates() {
        return locatable.getCoordinates();
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableObject2D)) return false;

        ImmutableObject2D that = (ImmutableObject2D) o;

        if (Double.compare(that.orientation, orientation) != 0) return false;
        if (!locatable.equals(that.locatable)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = orientation != +0.0d ? Double.doubleToLongBits(orientation) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + locatable.hashCode();
        return result;
    }

    public static ImmutableObject2D copyOf(Object2D object2D) {
        return new ImmutableObject2D(ImmutableLocation2D.at(object2D.getX(), object2D.getY()), object2D.getOrientationAngle());
    }

    public static Object2D of(double x, double y, double orientationAngle) {
        return new ImmutableObject2D(ImmutableLocation2D.at(x, y), orientationAngle);
    }
}
