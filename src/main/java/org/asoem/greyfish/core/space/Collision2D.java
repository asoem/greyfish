package org.asoem.greyfish.core.space;

import org.asoem.greyfish.utils.space.Point2D;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Object2D;

/**
 * User: christoph
 * Date: 25.06.12
 * Time: 14:14
 */
public class Collision2D {
    private final Object2D o1;
    private final Motion2D m1;
    private final Object2D o2;
    private final Motion2D m2;
    private final Point2D pointOfCollision;

    public Collision2D(Object2D o1, Motion2D m1, Object2D o2, Motion2D m2, Point2D pointOfCollision) {
        this.o1 = o1;
        this.m1 = m1;
        this.o2 = o2;
        this.m2 = m2;
        this.pointOfCollision = pointOfCollision;
    }

    public Object2D getO1() {
        return o1;
    }

    public Motion2D getM1() {
        return m1;
    }

    public Object2D getO2() {
        return o2;
    }

    public Motion2D getM2() {
        return m2;
    }

    public Point2D getPointOfCollision() {
        return pointOfCollision;
    }
}
