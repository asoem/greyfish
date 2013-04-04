package org.asoem.greyfish.utils.space;

/**
 * User: christoph
 * Date: 04.07.12
 * Time: 12:56
 */
public abstract class AbstractPoint2D implements Point2D {
    @Override
    public double[] getCoordinate() {
        return new double[] {getX(), getY()};
    }

    @Override
    public Double _1() {
        return getX();
    }

    @Override
    public Double _2() {
        return getY();
    }

    @Override
    public int getDimension() {
        return 2;
    }

    @Override
    public Point2D getCentroid() {
        return this;
    }
}
