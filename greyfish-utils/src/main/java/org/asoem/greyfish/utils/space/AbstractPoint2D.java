package org.asoem.greyfish.utils.space;


import static com.google.common.base.Preconditions.checkArgument;

public abstract class AbstractPoint2D implements Point2D {
    @Override
    public final double[] coordinates() {
        return new double[]{getX(), getY()};
    }

    @Override
    public final Double first() {
        return getX();
    }

    @Override
    public final Double second() {
        return getY();
    }

    @Override
    public final int getDimension() {
        return 2;
    }

    @Override
    public final Point2D getCentroid() {
        return this;
    }

    @Override
    public final double distance(final Point neighbor) {
        checkArgument(neighbor.getDimension() == 2, "Dimension mismatch");
        final double[] neighborCentroidCoordinate = neighbor.coordinates();
        return Geometry2D.distance(this.getX(), this.getY(),
                neighborCentroidCoordinate[0], neighborCentroidCoordinate[1]);
    }
}
