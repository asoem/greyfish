package org.asoem.greyfish.utils.space;

import javolution.lang.MathLib;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.simpleframework.xml.Attribute;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: christoph
 * Date: 18.10.11
 * Time: 16:26
 */
public class ImmutableObject2D implements Object2D {
    
    @Attribute(name = "x")
    private final double x;
    
    @Attribute(name = "y")
    private final double y;
    
    @Attribute(name = "orientation")
    private final double orientation;


    @SimpleXMLConstructor
    private ImmutableObject2D(@Attribute(name = "x") double x,
                              @Attribute(name = "y") double y,
                              @Attribute(name = "orientation") double orientation) {
        checkArgument(orientation >= 0 && orientation <= MathLib.TWO_PI, "Given angle is out of range [0, TWO_PI]: %s", orientation);
        this.orientation = orientation;
        this.x = x;
        this.y = y;
    }

    @Override
    public double getOrientationAngle() {
        return orientation;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double[] getCoordinates() {
        return new double[] {x,y};
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
        if (o == null || getClass() != o.getClass()) return false;

        ImmutableObject2D that = (ImmutableObject2D) o;

        if (Double.compare(that.orientation, orientation) != 0) return false;
        if (Double.compare(that.x, x) != 0) return false;
        if (Double.compare(that.y, y) != 0) return false;

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
        temp = orientation != +0.0d ? Double.doubleToLongBits(orientation) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ImmutableObject2D{" +
                "x=" + x +
                ", y=" + y +
                ", orientation=" + orientation +
                '}';
    }

    public static ImmutableObject2D copyOf(Object2D object2D) {
        return new ImmutableObject2D(object2D.getX(), object2D.getY(), object2D.getOrientationAngle());
    }

    public static ImmutableObject2D of(double x, double y, double orientationAngle) {
        return new ImmutableObject2D(x, y, orientationAngle);
    }
}
