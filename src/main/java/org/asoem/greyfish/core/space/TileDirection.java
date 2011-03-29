package org.asoem.greyfish.core.space;


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
}
