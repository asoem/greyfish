package org.asoem.greyfish.utils.space;

import javolution.lang.MathLib;

public class MutableMovable implements Movable {

    private ImmutableMotionVector motionVector = new ImmutableMotionVector(0, 0);

    public MutableMovable() {}

    public void setRotation(double alpha) {
        setMotion(alpha % MathLib.PI, getTranslation());
    }

    public void setTranslation(double translation) {
        setMotion(getRotation(), translation);
    }

    @Override
    public double getTranslation() {
        return motionVector.getRadius();
    }

    @Override
    public double getRotation() {
        return motionVector.getAngle();
    }

    /**
     * Change this object's motion vector by rotating it {@code angle}*PI degrees
     * and adding {@code velocity} to it's length
     * @param angle The value to sum to the angle of this object's motion vector
     * @param velocity The value to sum to the length of this object's motion vector
     */
    public void changeMotion(double angle, double velocity) {
        motionVector = motionVector.moved(angle, velocity);
    }

    /**
     * Change this object's motion vector by rotating it's orientation to {@code angle}*PI degrees
     * and it's length to {@code velocity}
     * @param angle The new value of the angle of this object's motion vector
     * @param velocity The new value of the length of this object's motion vector
     */
    public void setMotion(double angle, double velocity) {
        motionVector = ImmutableMotionVector.of(angle, velocity);
    }
}
