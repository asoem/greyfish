package org.asoem.greyfish.core.space;


import org.asoem.greyfish.utils.space.Coordinates2D;

public enum TileDirection {
    CENTER      ( 0, 0),
    NORTH       (-1, 0),
    NORTHEAST   (-1, 1),
    EAST        ( 0, 1),
    SOUTHEAST   ( 1, 1),
    SOUTH       ( 1, 0),
    SOUTHWEST   ( 1,-1),
    WEST        ( 0,-1),
    NORTHWEST   (-1,-1);

    final int xTranslation;
    final int yTranslation;

    TileDirection(int yTranslation, int xTranslation) {
        this.xTranslation = xTranslation;
        this.yTranslation = yTranslation;
    }

    public TileDirection opposite() {
        switch (this) {
            case CENTER:    return CENTER;
            case NORTH:     return SOUTH;
            case NORTHEAST: return SOUTHWEST;
            case EAST:      return WEST;
            case SOUTHEAST: return NORTHWEST;
            case SOUTH:     return NORTH;
            case SOUTHWEST: return NORTHEAST;
            case WEST:      return EAST;
            case NORTHWEST: return SOUTHEAST;
            default:
                throw new AssertionError("Expected this to be unreachable code");
        }
    }

    public static TileDirection forVector(Coordinates2D origin, Coordinates2D destination) {
        return forDiff(
                (int) destination.getX() - (int) origin.getX(),
                (int) destination.getY() - (int) origin.getY());
    }

    public static TileDirection forTiles(TileLocation origin, TileLocation destination) {
        return forDiff(
                destination.getX() - origin.getX(),
                destination.getY() - origin.getY());
    }

    private static TileDirection forDiff(int xDiff, int yDiff) {
        if (xDiff == 0) {
            if (yDiff == 0) return CENTER;
            else if (yDiff < 0) return NORTH;
            else return SOUTH;
        }
        else if (xDiff < 0) {
            if (yDiff == 0) return WEST;
            else if (yDiff < 0) return NORTHWEST;
            else return SOUTHWEST;
        }
        else {
            if (yDiff == 0) return EAST;
            else if (yDiff < 0) return NORTHEAST;
            else return SOUTHEAST;
        }
    }
}
