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
