package org.asoem.greyfish.utils.space;

import javolution.lang.MathLib;
import org.asoem.greyfish.utils.math.ImmutablePolarPoint2D;
import org.asoem.greyfish.utils.math.PolarPoint2D;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 20.10.11
 * Time: 15:37
 */
public class ImmutableMotion2D implements Motion2D {

    @Element
    private final PolarPoint2D polarPoint2D;

    public ImmutableMotion2D(PolarPoint2D polarPoint2D) {
        this.polarPoint2D = checkNotNull(polarPoint2D);
    }

    public ImmutableMotion2D(double angle, double v) {
        this.polarPoint2D = ImmutablePolarPoint2D.of(angle, v);
    }

    @Deprecated
    public double getAngle() {
        return polarPoint2D.getAngle();
    }

    @Deprecated
    public double getRadius() {
        return polarPoint2D.getRadius();
    }

    public ImmutableMotion2D rotated(double phi) {
        return new ImmutableMotion2D((getAngle() + phi) % MathLib.TWO_PI, getRadius());
    }

    public ImmutableMotion2D translated(double distance) {
        return new ImmutableMotion2D(getAngle(), getRadius() + distance);
    }

    public ImmutableMotion2D moved(double phi, double distance) {
        return new ImmutableMotion2D((getAngle() + phi) % MathLib.TWO_PI, getRadius() + distance);
    }

    public static ImmutableMotion2D newInstance(double phi, double length) {
        return new ImmutableMotion2D(phi, length);
    }

    public static ImmutableMotion2D of(double angle, double velocity) {
        return new ImmutableMotion2D(angle, velocity);
    }

    @Override
    public double getRotation2D() {
        return polarPoint2D.getAngle();
    }

    @Override
    public int getDimension() {
        return 2;
    }

    @Override
    public double getTranslation() {
        return polarPoint2D.getRadius();
    }

    @Override
    public double[] getRotation() {
        return new double[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImmutableMotion2D that = (ImmutableMotion2D) o;

        if (!polarPoint2D.equals(that.polarPoint2D)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return polarPoint2D.hashCode();
    }

    public static Motion2D noMotion() {
        return NoMotion.INSTANCE;
    }

    public static ImmutableMotion2D translated(Motion2D motion, double speed) {
        checkNotNull(motion);
        return new ImmutableMotion2D(motion.getRotation2D(), motion.getTranslation() + speed);
    }

    private enum NoMotion implements Motion2D {
        INSTANCE;

        private final static double[] rotationVector = new double[0];

        @Override
        public double getRotation2D() {
            return 0;
        }

        @Override
        public int getDimension() {
            return 2;
        }

        @Override
        public double getTranslation() {
            return 0;
        }

        @Override
        public double[] getRotation() {
            return rotationVector;
        }
    }
}
