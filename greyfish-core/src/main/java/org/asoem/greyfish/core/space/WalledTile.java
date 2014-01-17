package org.asoem.greyfish.core.space;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import org.asoem.greyfish.utils.space.Geometry2D;
import org.asoem.greyfish.utils.space.Point2D;
import org.asoem.greyfish.utils.space.Tile;
import org.asoem.greyfish.utils.space.TileDirection;

import java.util.ArrayList;

import static org.asoem.greyfish.utils.space.TileDirection.*;

public class WalledTile implements Tile {

    private final int wallFlagsMask;
    private final int x;
    private final int y;
    private int wallFlags = 0;

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private WalledTile(final int x, final int y) {
        this.x = x;
        this.y = y;
        this.wallFlagsMask = 0;
    }

    WalledTile(final TiledSpace<?, ?, WalledTile> space, final int x, final int y) {
        this.x = x;
        this.y = y;

        int mask = 0;
        if (x == 0) {
            mask |= (1 << WEST.ordinal());
        }
        if (x == space.colCount() - 1) {
            mask |= (1 << EAST.ordinal());
        }
        if (y == 0) {
            mask |= (1 << NORTH.ordinal());
        }
        if (y == space.rowCount() - 1) {
            mask |= (1 << SOUTH.ordinal());
        }
        wallFlagsMask = mask;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    /**
     * Checks for a wall in the given {@code direction} of this tile and for a wall in the opposite {@code direction} of
     * the tile in the given direction if present. For combined directions (e.g. {@code NORTHWEST}) the function will
     * return {@code true} if there is a wall for any of the single directions (e.g. {@code NORTH} or {@code WEST})
     *
     * @param direction The direction to check
     * @return {@code true} if this location has a wall in the given {@code direction} or the location in the given
     * {@code direction}, if present, has a border in the opposite direction, {@code false} otherwise.
     */
    public boolean hasWall(final TileDirection direction) {

        switch (direction) {
            case CENTER:
                return false;

            case NORTH:
            case SOUTH:
            case WEST:
            case EAST:
                if (isWallFlagSet(1 << direction.ordinal())) {
                    return true;
                }
                break;

            case NORTHEAST:
                if (isWallFlagSet(1 << NORTH.ordinal() | 1 << EAST.ordinal())) {
                    return true;
                }
                break;
            case SOUTHEAST:
                if (isWallFlagSet(1 << SOUTH.ordinal() | 1 << EAST.ordinal())) {
                    return true;
                }
                break;
            case SOUTHWEST:
                if (isWallFlagSet(1 << SOUTH.ordinal() | 1 << WEST.ordinal())) {
                    return true;
                }
                break;
            case NORTHWEST:
                if (isWallFlagSet(1 << NORTH.ordinal() | 1 << WEST.ordinal())) {
                    return true;
                }
                break;
        }

        return false;
    }

    private boolean isWallFlagSet(final int flags) {
        return ((wallFlags | wallFlagsMask) & flags) != 0;
    }

    public void setWall(final TileDirection direction, final boolean b) {
        switch (direction) {
            case CENTER:
                throw new IllegalArgumentException("A border at CENTER makes no sense");
            case NORTHEAST:
                setWall(NORTH, b);
                setWall(SOUTH, b);
                return;
            case SOUTHEAST:
                setWall(SOUTH, b);
                setWall(EAST, b);
                return;
            case SOUTHWEST:
                setWall(SOUTH, b);
                setWall(WEST, b);
                return;
            case NORTHWEST:
                setWall(NORTH, b);
                setWall(WEST, b);
                return;
            default:
                break;
        }

        if (b) {
            wallFlags |= (1 << direction.ordinal());
        } else {
            wallFlags &= (~(1 << direction.ordinal()));
        }
    }

    public int getWallFlags() {
        return wallFlags;
    }

    public void setWallFlags(final int borderFlags) {
        this.wallFlags = borderFlags;
    }

    public void toggleWall(final TileDirection direction) {
        setWall(direction, !hasWall(direction));
    }

    public boolean covers(final Point2D locatable) {
        return covers(locatable.getX(), locatable.getY());
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
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WalledTile other = (WalledTile) obj;
        return x == other.x && y == other.y;
    }

    @Override
    public String toString() {
        final ArrayList<String> walls = Lists.newArrayList();

        if (hasWall(NORTH)) {
            walls.add("N");
        }
        if (hasWall(NORTHEAST)) {
            walls.add("NE");
        }
        if (hasWall(EAST)) {
            walls.add("E");
        }
        if (hasWall(SOUTHEAST)) {
            walls.add("SE");
        }
        if (hasWall(SOUTH)) {
            walls.add("S");
        }
        if (hasWall(SOUTHWEST)) {
            walls.add("SW");
        }
        if (hasWall(WEST)) {
            walls.add("W");
        }
        if (hasWall(NORTHWEST)) {
            walls.add("NW");
        }

        return "[" + Doubles.join(",", x, y) + "] (border:" + Joiner.on(",").join(walls) + ")";
    }

    public boolean covers(final double x, final double y) {
        return Geometry2D.rectangleContains(getX(), getY(), 1, 1, x, y);
    }
}
