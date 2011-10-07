package org.asoem.greyfish.core.space;


import org.asoem.greyfish.utils.PolarPoint;

public interface MovingObject2D extends Object2D {

    public PolarPoint getMotionVector();

    public void setMotionVector(PolarPoint polarPoint);

    /**
     * Change this object's motion vector by rotating it {@code angle}*PI degrees
     * and adding {@code velocity} to it's length
     * @param angle The value to sum to the angle of this object's motion vector
     * @param velocity The value to sum to the length of this object's motion vector
     */
    public void changeMotion(double angle, double velocity);

    /**
     * Change this object's motion vector by rotating it's orientation to {@code angle}*PI degrees
     * and it's length to {@code velocity}
     * @param angle The new value of the angle of this object's motion vector
     * @param velocity The new value of the length of this object's motion vector
     */
    public void setMotion(double angle, double velocity);

    public void setAnchorPoint(Coordinates2D coordinates2d);

    public void setOrientation(double alpha);
}
