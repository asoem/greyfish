package org.asoem.greyfish.core.space;

/**
* Created by IntelliJ IDEA.
* User: christoph
* Date: 28.03.11
* Time: 20:05
* To change this template use File | Settings | File Templates.
*/
public enum Direction {
    CENTER(0,0,0),
    NORTH(-1,0,TileLocation.BORDER_NORTH),
    SOUTH(1,0,TileLocation.BORDER_SOUTH),
    EAST(0,1,TileLocation.BORDER_EAST),
    WEST(0,-1,TileLocation.BORDER_WEST),
    NORTHEAST(-1,1,TileLocation.BORDER_NORTH | TileLocation.BORDER_EAST),
    SOUTHWEST(1,-1,TileLocation.BORDER_SOUTH | TileLocation.BORDER_WEST),
    NORTHWEST(-1,-1,TileLocation.BORDER_NORTH | TileLocation.BORDER_WEST),
    SOUTHEAST(1,1,TileLocation.BORDER_SOUTH | TileLocation.BORDER_EAST);
    // CAVE! Order matters for the opposite() function

    final int xTranslation;
    final int yTranslation;
    final int borderCheck;

    Direction(int yTranslation, int xTranslation, int borderCheck) {
        this.xTranslation = xTranslation;
        this.yTranslation = yTranslation;
        this.borderCheck = borderCheck;
    }

    public Direction opposite() {
        if (this == CENTER)
            return CENTER;
        if ((this.ordinal() & 1) != 0) // odd
            return Direction.values()[this.ordinal()+1];
        else
            return Direction.values()[this.ordinal()-1];
    }
}
