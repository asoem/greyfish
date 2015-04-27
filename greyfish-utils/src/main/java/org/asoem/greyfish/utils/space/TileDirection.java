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


public enum TileDirection {
    CENTER(0, 0),
    NORTH(-1, 0),
    NORTHEAST(-1, 1),
    EAST(0, 1),
    SOUTHEAST(1, 1),
    SOUTH(1, 0),
    SOUTHWEST(1, -1),
    WEST(0, -1),
    NORTHWEST(-1, -1);

    private final int xTranslation;
    private final int yTranslation;

    TileDirection(final int yTranslation, final int xTranslation) {
        this.xTranslation = xTranslation;
        this.yTranslation = yTranslation;
    }

    public TileDirection opposite() {
        switch (this) {
            case CENTER:
                return CENTER;
            case NORTH:
                return SOUTH;
            case NORTHEAST:
                return SOUTHWEST;
            case EAST:
                return WEST;
            case SOUTHEAST:
                return NORTHWEST;
            case SOUTH:
                return NORTH;
            case SOUTHWEST:
                return NORTHEAST;
            case WEST:
                return EAST;
            case NORTHWEST:
                return SOUTHEAST;
            default:
                throw new AssertionError("Expected this to be unreachable code");
        }
    }

    public static TileDirection forAdjacentTiles(final Tile origin, final Tile destination) {
        final int xDiff = destination.getX() - origin.getX();
        final int yDiff = destination.getY() - origin.getY();

        if (Math.abs(xDiff) > 1 || Math.abs(yDiff) > 1) {
            throw new IllegalArgumentException("Cannot calculate direction for non adjacent tiles: origin=" + origin + ", destination=" + destination);
        }

        if (xDiff == 0) {
            if (yDiff == 0) {
                return CENTER;
            } else if (yDiff == -1) {
                return NORTH;
            } else {
                return SOUTH;
            }
        } else if (xDiff == -1) {
            if (yDiff == 0) {
                return WEST;
            } else if (yDiff == -1) {
                return NORTHWEST;
            } else {
                return SOUTHWEST;
            }
        } else {
            if (yDiff == 0) {
                return EAST;
            } else if (yDiff == -1) {
                return NORTHEAST;
            } else {
                return SOUTHEAST;
            }
        }
    }

    public int getXTranslation() {
        return xTranslation;
    }

    public int getYTranslation() {
        return yTranslation;
    }
}
