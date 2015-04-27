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

@Immutable
public final class ImmutableObject2D implements Object2D {

    private final Point2D anchorPoint;

    private ImmutableObject2D(final Point2D anchorPoint) {
        this.anchorPoint = ImmutablePoint2D.copyOf(anchorPoint);
    }

    @Override
    public int getDimension() {
        return 2;
    }

    @Override
    public Point2D getCentroid() {
        return anchorPoint;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ImmutableObject2D that = (ImmutableObject2D) o;

        if (!anchorPoint.equals(that.anchorPoint)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return anchorPoint.hashCode();
    }

    public static ImmutableObject2D copyOf(final Object2D object2D) {
        return new ImmutableObject2D(object2D.getCentroid());
    }

    public static ImmutableObject2D of(final double x, final double y) {
        return new ImmutableObject2D(ImmutablePoint2D.at(x, y));
    }
}
