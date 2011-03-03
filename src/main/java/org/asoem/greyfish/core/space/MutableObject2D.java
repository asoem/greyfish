package org.asoem.greyfish.core.space;

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
    public Location2D getAnchorPoint() {
        return ImmutableLocation2D.at(x, y);
    }

    @Override
    public void setAnchorPoint(Location2D location2d) {
        checkNotNull(location2d);
        x = location2d.getX();
        y = location2d.getY();
    }

    @Override
    public double getOrientation() {
        return orientation;
    }

    @Override
    public void setOrientation(double alpha) {
        checkArgument(alpha > 0 && alpha <= MathLib.PI);
        this.orientation = alpha;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    public static MutableObject2D at() {
        return new MutableObject2D(0, 0, 0);
    }

    public static MutableObject2D at(double x, double y) {
        return new MutableObject2D(x, y, 0);
    }

    public static MutableObject2D at(double x, double y, double angle) {
        return new MutableObject2D(x, y, angle);
    }
}
