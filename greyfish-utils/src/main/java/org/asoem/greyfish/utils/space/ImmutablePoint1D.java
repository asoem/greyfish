/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.utils.space;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkPositionIndex;

/**
 * ImmutablePoint1D is an immutable implementation of a point in an 1D vector space.
 */
@Immutable
public final class ImmutablePoint1D implements Point1D {
    private final double x;

    private ImmutablePoint1D(final double x) {
        this.x = x;
    }

    public static ImmutablePoint1D at(final double x) {
        return new ImmutablePoint1D(x);
    }

    @Override
    public double[] coordinates() {
        return new double[]{x};
    }

    @Override
    public double get(final int index) {
        checkPositionIndex(index, getDimension());
        switch (index) {
            case 0:
                return getX();
            default:
                throw new AssertionError("unreachable");
        }
    }

    @Override
    public double distance(final Point point) {
        checkArgument(point.getDimension() == 1,
                "Dimension mismatch: %s != %s", point.getDimension(), this.getDimension());
        return Math.abs(point.get(0) - x);
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
