package org.asoem.greyfish.utils.space;

import javolution.lang.MathLib;

public class MutableMovable implements Movable {

    private double orientationAngle = 0;

    private ImmutableMotionVector motionVector = new ImmutableMotionVector(0, 0);

    public MutableMovable() {}

    @Override
    public void setOrientation(double alpha) {
        orientationAngle = (orientationAngle + alpha) % MathLib.PI;
    }

    @Override
    public MotionVector2D getMotionVector() {
        return motionVector;
    }

    @Override
    public void changeMotion(double angle, double velocity) {
        motionVector = motionVector.moved(angle, velocity);
    }

    @Override
    public void setMotion(double angle, double velocity) {
        motionVector = ImmutableMotionVector.of(angle, velocity);
    }
}
