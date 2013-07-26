package org.asoem.greyfish.utils.space;

import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * User: christoph
 * Date: 20.10.11
 * Time: 15:37
 */
public class ImmutableMotion2D implements Motion2D, Serializable {

    private final double translation;

    private final double rotation;

    public ImmutableMotion2D(final double angle, final double v) {
        this.translation = v;
        this.rotation = angle;
    }

    @SuppressWarnings("UnusedDeclaration")
    public ImmutableMotion2D rotated(final double phi) {
        return new ImmutableMotion2D(getRotation() + phi, getTranslation());
    }

    @SuppressWarnings("UnusedDeclaration")
    public ImmutableMotion2D translated(final double distance) {
        return new ImmutableMotion2D(getRotation(), getTranslation() + distance);
    }

    public ImmutableMotion2D modified(final double phi, final double distance) {
        return new ImmutableMotion2D(getRotation() + phi, getTranslation() + distance);
    }

    public static ImmutableMotion2D newInstance(final double phi, final double length) {
        return new ImmutableMotion2D(phi, length);
    }

    public static ImmutableMotion2D of(final double angle, final double velocity) {
        return new ImmutableMotion2D(angle, velocity);
    }

    @Override
    public double getRotation() {
        return rotation;
    }

    @Override
    public int getDimension() {
        return 2;
    }

    @Override
    public double getTranslation() {
        return translation;
    }

    @Override
    public double[] getRotationAngles() {
        return new double[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ImmutableMotion2D that = (ImmutableMotion2D) o;

        if (Double.compare(that.rotation, rotation) != 0) return false;
        if (Double.compare(that.translation, translation) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = translation != +0.0d ? Double.doubleToLongBits(translation) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = rotation != +0.0d ? Double.doubleToLongBits(rotation) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(translation).addValue(rotation).toString();
    }

    public static Motion2D noMotion() {
        return NoMotion.INSTANCE;
    }

    private enum NoMotion implements Motion2D {
        INSTANCE;

        private final static double[] rotationVector = new double[0];

        @Override
        public double getRotation() {
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
        public double[] getRotationAngles() {
            return rotationVector;
        }
    }

    private static final long serialVersionUID = 0;
}
