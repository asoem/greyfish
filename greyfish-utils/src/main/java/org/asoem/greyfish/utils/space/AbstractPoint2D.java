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
