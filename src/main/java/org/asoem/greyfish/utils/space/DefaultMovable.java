package org.asoem.greyfish.utils.space;

import javolution.lang.MathLib;
import org.simpleframework.xml.Element;

public class DefaultMovable implements Movable {

    @Element(name="point")
    protected final MutableCoordinates2D anchorPoint;

    private double orientationAngle;

    private ImmutableMotionVector motionVector = new ImmutableMotionVector(0, 0);

    public DefaultMovable() {
        anchorPoint = new MutableCoordinates2D();
    }

    protected DefaultMovable(@Element(name = "point") MutableCoordinates2D anchorPoint) {
        this.anchorPoint = anchorPoint;
    }

    public Coordinates2D getCoordinates() {
        return new MutableCoordinates2D(anchorPoint);
    }

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
