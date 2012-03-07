package org.asoem.greyfish.utils.space;

import org.simpleframework.xml.Attribute;

/**
 * User: christoph
 * Date: 20.10.11
 * Time: 15:37
 */
public class ImmutableMotion2D implements Motion2D {

    @Attribute(name = "translation")
    private final double translation;

    @Attribute(name = "rotation")
    private final double rotation;

    public ImmutableMotion2D(@Attribute(name = "rotation") double angle,
                             @Attribute(name = "translation") double v) {
        this.translation = v;
        this.rotation = angle;
    }

    @SuppressWarnings("UnusedDeclaration")
    public ImmutableMotion2D rotated(double phi) {
        return new ImmutableMotion2D(getRotation() + phi, getTranslation());
    }

    @SuppressWarnings("UnusedDeclaration")
    public ImmutableMotion2D translated(double distance) {
        return new ImmutableMotion2D(getRotation(), getTranslation() + distance);
    }

    public ImmutableMotion2D modified(double phi, double distance) {
        return new ImmutableMotion2D(getRotation() + phi, getTranslation() + distance);
    }

    public static ImmutableMotion2D newInstance(double phi, double length) {
        return new ImmutableMotion2D(phi, length);
    }

    public static ImmutableMotion2D of(double angle, double velocity) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImmutableMotion2D that = (ImmutableMotion2D) o;

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
}
