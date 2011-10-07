package org.asoem.greyfish.utils;

import javolution.lang.MathLib;
import org.asoem.greyfish.core.space.Coordinates2D;
import org.asoem.greyfish.core.space.MutableCoordinates2D;

public class PolarPoint {

    final double angle;
    final double distance;

    public PolarPoint(double angle, double distance) {
        this.angle = angle;
        this.distance = distance;
    }

    public double getAngle() {
        return angle;
    }

    public double getDistance() {
        return distance;
    }

    public PolarPoint rotated(double phi) {
        return new PolarPoint((this.angle + phi) % MathLib.TWO_PI, distance);
    }

    public PolarPoint translated(double distance) {
        return new PolarPoint(angle, this.distance + distance);
    }

    public PolarPoint moved(double phi, double distance) {
        return new PolarPoint((this.angle + phi) % MathLib.TWO_PI, this.distance + distance);
    }

    public static PolarPoint newInstance(double phi, double length) {
        return new PolarPoint(phi, length);
    }

    public Coordinates2D toCartesian() {
        return MutableCoordinates2D.at(
                distance * Math.cos(angle),
                distance * Math.sin(angle)
        );
    }
}
