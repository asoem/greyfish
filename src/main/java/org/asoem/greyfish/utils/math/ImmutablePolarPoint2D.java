package org.asoem.greyfish.utils.math;

public class ImmutablePolarPoint2D implements PolarPoint2D {

    final double angle;
    final double distance;

    public ImmutablePolarPoint2D(double angle, double distance) {
        this.angle = angle;
        this.distance = distance;
    }

    @Override
    public double getAngle() {
        return angle;
    }

    @Override
    public double getRadius() {
        return distance;
    }

    public static PolarPoint2D of(double angle, double v) {
        return new ImmutablePolarPoint2D(angle,v);
    }
}
