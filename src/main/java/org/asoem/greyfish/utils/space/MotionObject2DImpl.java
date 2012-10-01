package org.asoem.greyfish.utils.space;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * User: christoph
 * Date: 03.07.12
 * Time: 12:36
 */
public class MotionObject2DImpl extends ForwardingObject2D implements MotionObject2D {

    @Attribute(name = "collision")
    private final boolean collision;

    private final ImmutableObject2D delegate;

    public MotionObject2DImpl(@Element(name = "anchorPoint") Point2D anchorPoint,
                              @Attribute(name = "orientation") double orientation,
                              @Attribute(name = "collision") boolean collision) {
        this.delegate = ImmutableObject2D.of(anchorPoint.getX(), anchorPoint.getY(), orientation);
        this.collision = collision;
    }

    @Override
    protected Object2D delegate() {
        return delegate;
    }

    @Override
    public boolean didCollide() {
        return collision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MotionObject2DImpl that = (MotionObject2DImpl) o;

        if (collision != that.collision) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (collision ? 1 : 0);
        return result;
    }

    public static MotionObject2D of(double x, double y, double newOrientation, boolean b) {
        return new MotionObject2DImpl(ImmutablePoint2D.at(x, y), newOrientation, b);
    }

    public static MotionObject2D copyOf(MotionObject2D projection) {
        return new MotionObject2DImpl(projection.getAnchorPoint(), projection.getOrientationAngle(), false);
    }

    public static MotionObject2D reorientated(MotionObject2D projection, double angle) {
        return new MotionObject2DImpl(projection.getAnchorPoint(), angle, false);
    }
}
