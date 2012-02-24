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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImmutablePolarPoint2D that = (ImmutablePolarPoint2D) o;

        return Double.compare(that.angle, angle) == 0 && Double.compare(that.distance, distance) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = angle != +0.0d ? Double.doubleToLongBits(angle) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = distance != +0.0d ? Double.doubleToLongBits(distance) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public static PolarPoint2D of(double angle, double v) {
        return new ImmutablePolarPoint2D(angle,v);
    }
}
