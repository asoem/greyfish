package org.asoem.greyfish.utils.space;

import static com.google.common.base.Preconditions.checkArgument;

public final class ImmutablePoint1D implements Point, Point1D {
    private final double x;

    public ImmutablePoint1D(final double x) {
        this.x = x;
    }

    @Override
    public double[] getCoordinate() {
        return new double[]{x};
    }

    @Override
    public double distance(final Point point) {
        checkArgument(point.getDimension() == 1);
        return Math.abs(point.getCoordinate()[0] - x);
    }

    @Override
    public int getDimension() {
        return 1;
    }

    @Override
    public Point getCentroid() {
        return this;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ImmutablePoint1D point1D = (ImmutablePoint1D) o;

        if (Double.compare(point1D.x, x) != 0) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final long temp = Double.doubleToLongBits(x);
        return (int) (temp ^ (temp >>> 32));
    }
}
