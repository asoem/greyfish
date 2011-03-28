package org.asoem.greyfish.core.space;

import com.google.common.primitives.Doubles;
import javolution.util.FastList;
import org.simpleframework.xml.Attribute;

import java.util.List;

import static org.asoem.greyfish.core.space.Direction.*;

public class TileLocation {

    @Attribute
    private final int x;

    @Attribute
    private final int y;

    final public static int BORDER_NORTH = 1;
    final public static int BORDER_WEST = 2;
    final public static int BORDER_SOUTH = 4;
    final public static int BORDER_EAST = 8;

    private final List<MovingObject2D> occupants = FastList.newInstance();

    private final TiledSpace space;

    private int borderFlags = 0;
    private final int borderFlagsMask;

    TileLocation(TiledSpace space, int x, int y) {
        this.space = space;
        this.x = x;
        this.y = y;

        int mask = 0;
        if (x == 0) mask |= BORDER_WEST;
        if (x == space.getWidth() -1) mask |= BORDER_EAST;
        if (y == 0) mask |= BORDER_NORTH;
        if (y == space.getHeight() -1) mask |= BORDER_SOUTH;
        borderFlagsMask = mask;
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TileLocation other = (TileLocation) obj;
        return x == other.x && y == other.y;
    }

    @Override
    public String toString() {
        return "[" + Doubles.join(",",x,y) + "] (border:" +
                (hasBorder(BORDER_NORTH) ? "N" : "") +
                (hasBorder(BORDER_EAST) ? "E" : "") +
                (hasBorder(BORDER_SOUTH) ? "S" : "") +
                (hasBorder(BORDER_WEST) ? "W" : "") +
                ")";
    }

    public boolean hasBorder(Direction direction) {
        return direction != CENTER && hasBorder(direction.borderCheck);
    }

    public boolean hasBorder(int flags) {
        return ((borderFlags | borderFlagsMask) & flags) != 0;
    }

    public void setBorder(Direction direction, boolean b) {
        if (b) {
            borderFlags |= direction.borderCheck;
            if (hasNeighbourTile(direction))
                getNeighbourTile(direction).borderFlags |= direction.opposite().borderCheck;
        } else {
            borderFlags &= (~direction.borderCheck);
            if (hasNeighbourTile(direction))
                getNeighbourTile(direction).borderFlags &= (~direction.opposite().borderCheck);
        }
    }

    TileLocation getNeighbourTile(Direction direction) {
        return space.getLocationAt(getX() + direction.xTranslation, getY() + direction.yTranslation);
    }

    boolean hasNeighbourTile(Direction direction) {
        return space.hasLocationAt(getX() + direction.xTranslation, getY() + direction.yTranslation);
    }

    public Iterable<MovingObject2D> getOccupants() {
        return occupants;
    }

    /**
     * @param l2 TileLocation 2
     * @return {@code true} if {@code l2} is adjacent to and reachable from this
     */
    public boolean hasReachableNeighbour(TileLocation l2) {
        return isNeighbourTile(l2) && !hasBorder(computeDirection(this, l2));
    }

    private static Direction computeDirection(TileLocation tileLocation, TileLocation l2) {
        final int xDiff = l2.getX() - tileLocation.getX();
        final int yDiff = l2.getY() - tileLocation.getY();

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
        else if (xDiff > 0) {
            if (yDiff == 0) return EAST;
            else if (yDiff < 0) return NORTHEAST;
            else return SOUTHEAST;
        }

        return null;
    }

    private boolean isNeighbourTile(TileLocation l2) {
        return Math.abs(x - l2.x) <= 1 && Math.abs(y - l2.y) <= 1;
    }

    public int getBorderFlags() {
        return borderFlags;
    }

    public void setBorderFlags(int borderFlags) {
        this.borderFlags = borderFlags;
    }

    public boolean hasBorder(TileLocation location) {
        return hasBorder(computeDirection(this, location));
    }

    public void removeAllOccupants() {
        occupants.clear();
    }

    public void addOccupant(MovingObject2D object2d) {
        occupants.add(object2d);
    }

    public boolean removeOccupant(MovingObject2D individual) {
        return occupants.remove(individual);
    }
}
