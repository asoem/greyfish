package org.asoem.greyfish.utils.space;


import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkPositionIndex;

public abstract class AbstractPoint2D implements Point2D {

    @Override
    public final Double first() {
        return getX();
    }

    @Override
    public final Double second() {
        return getY();
    }

    @Override
    public final double get(final int index) {
        checkPositionIndex(index, getDimension());
        switch (index) {
            case 0:
                return getX();
            case 1:
                return getY();
            default:
                throw new AssertionError("unreachable");
        }
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
    public final double distance(final Point other) {
        checkArgument(other.getDimension() == this.getDimension(),
                "Dimension mismatch: %s != %s", other.getDimension(), this.getDimension());
        return Geometry2D.distance(
                this.get(0), this.get(1),
                other.get(0), other.get(1));
    }
}
