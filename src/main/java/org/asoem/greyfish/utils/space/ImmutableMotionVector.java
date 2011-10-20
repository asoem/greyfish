package org.asoem.greyfish.utils.space;

import javolution.lang.MathLib;
import org.asoem.greyfish.utils.math.ImmutablePolarPoint2D;
import org.asoem.greyfish.utils.math.PolarPoint2D;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 20.10.11
 * Time: 15:37
 */
public class ImmutableMotionVector implements MotionVector2D {
    private final PolarPoint2D polarPoint2D;

    public ImmutableMotionVector(PolarPoint2D polarPoint2D) {
        this.polarPoint2D = checkNotNull(polarPoint2D);
    }

    public ImmutableMotionVector(double angle, double v) {
        this.polarPoint2D = ImmutablePolarPoint2D.of(angle, v);
    }

    @Override
    public double getAngle() {
        return polarPoint2D.getAngle();
    }

    @Override
    public double getDistance() {
        return polarPoint2D.getDistance();
    }

    public PolarPoint2D rotated(double phi) {
        return new ImmutableMotionVector((getAngle() + phi) % MathLib.TWO_PI, getDistance());
    }

    public PolarPoint2D translated(double distance) {
        return new ImmutableMotionVector(getAngle(), getDistance() + distance);
    }

    public ImmutableMotionVector moved(double phi, double distance) {
        return new ImmutableMotionVector((getAngle() + phi) % MathLib.TWO_PI, getDistance() + distance);
    }

    public static ImmutableMotionVector newInstance(double phi, double length) {
        return new ImmutableMotionVector(phi, length);
    }

    public static ImmutableMotionVector of(double angle, double velocity) {
        return new ImmutableMotionVector(angle, velocity);
    }
}
