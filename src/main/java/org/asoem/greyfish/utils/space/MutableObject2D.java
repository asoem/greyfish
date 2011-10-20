package org.asoem.greyfish.utils.space;

import javolution.lang.MathLib;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.simpleframework.xml.Attribute;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 03.03.11
 * Time: 11:29
 */
public class MutableObject2D implements Object2D {

    @Attribute(name = "x")
    private double x;

    @Attribute(name = "y")
    private double y;

    @Attribute(name = "orientation")
    private double orientation = 0;

    @SimpleXMLConstructor
    private MutableObject2D() {
    }

    public MutableObject2D(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.orientation = angle;
    }

    @Override
    public Coordinates2D getCoordinates() {
        return ImmutableCoordinates2D.at(x, y);
    }

    public void setCoordinates(Coordinates2D coordinates2d) {
        checkNotNull(coordinates2d);
        x = coordinates2d.getX();
        y = coordinates2d.getY();
    }

    @Override
    public double getOrientation() {
        return orientation;
    }

    public void setOrientation(double alpha) {
        checkArgument(alpha > 0 && alpha <= MathLib.PI);
        this.orientation = alpha;
    }

    public static MutableObject2D locatedAt(double x, double y) {
        return new MutableObject2D(x, y, 0);
    }

    public static MutableObject2D at(double x, double y, double angle) {
        return new MutableObject2D(x, y, angle);
    }

    public static MutableObject2D at(Coordinates2D coordinates2D, double angle) {
        return new MutableObject2D(coordinates2D.getX(), coordinates2D.getY(), angle);
    }
}
