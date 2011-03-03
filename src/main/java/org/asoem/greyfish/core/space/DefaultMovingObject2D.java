package org.asoem.greyfish.core.space;

import javolution.lang.MathLib;
import org.asoem.greyfish.utils.PolarPoint;
import org.simpleframework.xml.Element;

public class DefaultMovingObject2D implements MovingObject2D {

    @Element(name="point")
    protected final MutableLocation2D anchorPoint;

    private double orientationAngle;

    private PolarPoint motionVector = new PolarPoint(0, 0);

    public DefaultMovingObject2D() {
        anchorPoint = new MutableLocation2D();
    }

    protected DefaultMovingObject2D(@Element(name = "point") MutableLocation2D anchorPoint) {
        this.anchorPoint = anchorPoint;
    }

    public Location2D getAnchorPoint() {
        return new MutableLocation2D(anchorPoint);
    }

    @Override
    public void setAnchorPoint(Location2D location2d) {
        setAnchorPoint(location2d.getX(), location2d.getY());
    }

    void setAnchorPoint(double x, double y) {
        anchorPoint.set(x, y);
    }

    @Override
    public double getOrientation() {
        return orientationAngle;
    }

    @Override
    public void setOrientation(double alpha) {
        orientationAngle = (orientationAngle + alpha) % MathLib.PI;
    }

    @Override
    public PolarPoint getMotionVector() {
        return motionVector;
    }

    @Override
    public void setMotionVector(PolarPoint polarPoint) {
        this.motionVector = polarPoint;
    }

    @Override
    public void changeMotion(double angle, double velocity) {
        motionVector = motionVector.moved(angle, velocity);
    }

    @Override
    public void setMotion(double angle, double velocity) {
        motionVector = PolarPoint.newInstance(angle, velocity);
    }

    @Override
    public double getX() {
        return anchorPoint.getX();
    }

    @Override
    public double getY() {
        return anchorPoint.getY();
    }
}
