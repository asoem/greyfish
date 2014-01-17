package org.asoem.greyfish.utils.space;

/**
 * User: christoph Date: 03.07.12 Time: 12:36
 */
public class MotionObject2DImpl extends ForwardingObject2D implements Object2D, Moving2D {

    private final boolean collision;
    private final ImmutableObject2D delegate;

    public MotionObject2DImpl(final Point2D anchorPoint, final boolean collision) {
        this.delegate = ImmutableObject2D.of(anchorPoint.getX(), anchorPoint.getY());
        this.collision = collision;
    }

    @Override
    protected Object2D delegate() {
        return delegate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final MotionObject2DImpl that = (MotionObject2DImpl) o;

        if (collision != that.collision) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (collision ? 1 : 0);
        return result;
    }

    public static MotionObject2DImpl of(final double x, final double y, final boolean b) {
        return new MotionObject2DImpl(ImmutablePoint2D.at(x, y), b);
    }

    public static MotionObject2DImpl copyOf(final MotionObject2D projection) {
        return new MotionObject2DImpl(projection.getCentroid(), false);
    }

    public static MotionObject2DImpl reorientated(final MotionObject2D projection) {
        return new MotionObject2DImpl(projection.getCentroid(), false);
    }

    public static MotionObject2DImpl of(final double x, final double y) {
        return of(x, y, false);
    }

    @Override
    public double getAngle() {
        return 0;
    }

    @Override
    public double getSpeed() {
        return 0;
    }

    @Override
    public double getAcceleration() {
        return 1;
    }
}
