package org.asoem.greyfish.utils.space;

import javolution.lang.MathLib;

public class MutableMotion2D implements Motion2D {

    private ImmutableMotion2D motion2D = new ImmutableMotion2D(0, 0);

    public MutableMotion2D() {}

    public void setRotation(double alpha) {
        setMotion(alpha % MathLib.PI, getTranslation());
    }

    public void setTranslation(double translation) {
        setMotion(getRotation(), translation);
    }

    @Override
    public int getDimension() {
        return 2;
    }

    @Override
    public double getTranslation() {
        return motion2D.getTranslation();
    }

    @Override
    public double[] getRotationAngles() {
        return motion2D.getRotationAngles();
    }

    @Override
    public double getRotation() {
        return motion2D.getRotation();
    }

    /**
     * Change this object's motion vector by rotating it {@code angle}*PI degrees
     * and adding {@code velocity} to it's length
     * @param angle The value to sum to the angle of this object's motion vector
     * @param velocity The value to sum to the length of this object's motion vector
     */
    public void changeMotion(double angle, double velocity) {
        motion2D = motion2D.modified(angle, velocity);
    }

    /**
     * Change this object's motion vector by rotating it's orientation to {@code angle}*PI degrees
     * and it's length to {@code velocity}
     * @param angle The new value of the angle of this object's motion vector
     * @param velocity The new value of the length of this object's motion vector
     */
    public void setMotion(double angle, double velocity) {
        motion2D = ImmutableMotion2D.of(angle, velocity);
    }
}
