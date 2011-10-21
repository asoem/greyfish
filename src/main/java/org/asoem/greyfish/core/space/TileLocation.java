package org.asoem.greyfish.core.space;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.utils.space.Coordinates2D;
import org.asoem.greyfish.utils.space.ImmutableCoordinates2D;
import org.asoem.greyfish.utils.space.Location2D;
import org.simpleframework.xml.Attribute;

import java.util.ArrayList;

import static org.asoem.greyfish.core.space.TileDirection.*;

public class TileLocation implements Location2D {

    // private final FastList<MovingObject2D> occupants = FastList.newInstance();

    private final TiledSpace space;

    private final int borderFlagsMask;

    @Attribute(name = "x")
    private final int x;

    @Attribute(name = "y")
    private final int y;

    @Attribute(name = "border", required = false)
    private int borderFlags = 0;

    @SimpleXMLConstructor
    private TileLocation(@Attribute(name = "x") int x, @Attribute(name = "y") int y) {
        this.x = x;
        this.y = y;
        this.borderFlagsMask = 0;
        this.space = null;
    }

    TileLocation(TiledSpace space, int x, int y) {
        this.space = space;
        this.x = x;
        this.y = y;

        int mask = 0;
        if (x == 0) mask |= (1 << WEST.ordinal());
        if (x == space.getWidth() -1) mask |= (1 << EAST.ordinal());
        if (y == 0) mask |= (1 << NORTH.ordinal());
        if (y == space.getHeight() -1) mask |= (1 << SOUTH.ordinal());
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

    /**
     * This method the same as calling {@link ImmutableCoordinates2D#at(double, double)} with arguments {@link #getX()} and {@link #getY()}
     * @return the coordinates of the top left corner of this location in the tiledSpace.
     */
    @Override
    public Coordinates2D getCoordinates() {
        return ImmutableCoordinates2D.at(x, y);
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
        ArrayList<String> borderList = Lists.newArrayList();
        if (hasBorder(NORTH)) borderList.add("N");
        if (hasBorder(NORTHEAST)) borderList.add("NE");
        if (hasBorder(EAST)) borderList.add("E");
        if (hasBorder(SOUTHEAST)) borderList.add("SE");
        if (hasBorder(SOUTH)) borderList.add("S");
        if (hasBorder(SOUTHWEST)) borderList.add("SW");
        if (hasBorder(WEST)) borderList.add("W");
        if (hasBorder(NORTHWEST)) borderList.add("NW");

        return "[" + Doubles.join(",",x,y) + "] (border:" + Joiner.on(",").join(borderList) + ")";
    }

    /**
     * Checks for a border in the given {@code direction} of this tile and for a border in the opposite {@code direction} of the tile in the given direction if present.
     * For combined directions (e.g. {@code NORTHWEST}) the function will return
     * {@code true} if there is a border for any of the single directions (e.g. {@code NORTH} or {@code WEST})
     * @param direction The direction to check
     * @return {@code true} if this location has a border in the given {@code direction}
     * or the location in the given {@code direction}, if present, has a border in the opposite direction,
     * {@code false} otherwise.
     */
    public boolean hasBorder(TileDirection direction) {
        return hasBorder(direction, true);
    }

    private boolean hasBorder(TileDirection direction, boolean checkBorderAtDestination) {

        switch (direction) {
            case CENTER:
                return false;

            case NORTH:
            case SOUTH:
            case WEST:
            case EAST:
                return hasBorder(1 << direction.ordinal());

            case NORTHEAST:
                if (hasBorder(1 << NORTH.ordinal() | 1 << EAST.ordinal())) return true;
            case SOUTHEAST:
                if (hasBorder(1 << SOUTH.ordinal() | 1 << EAST.ordinal())) return true;
            case SOUTHWEST:
                if (hasBorder(1 << SOUTH.ordinal() | 1 << WEST.ordinal())) return true;
            case NORTHWEST:
                if (hasBorder(1 << NORTH.ordinal() | 1 << WEST.ordinal())) return true;
        }

        return checkBorderAtDestination && getNeighbourTile(direction).hasBorder(direction.opposite(), false);
    }

    private boolean hasBorder(int flags) {
        return ((borderFlags | borderFlagsMask) & flags) != 0;
    }

    public void setBorder(TileDirection direction, boolean b) {
        switch (direction) {
            case CENTER:
                throw new IllegalArgumentException("A border at CENTER makes no sense");
            case NORTHEAST:
                setBorder(NORTH, b);
                setBorder(SOUTH, b);
                return;
            case SOUTHEAST:
                setBorder(SOUTH, b);
                setBorder(EAST, b);
                return;
            case SOUTHWEST:
                setBorder(SOUTH, b);
                setBorder(WEST, b);
                return;
            case NORTHWEST:
                setBorder(NORTH, b);
                setBorder(WEST, b);
                return;
            default:
                break;
        }

        if (b) {
            borderFlags |= (1 << direction.ordinal());
            if (hasNeighbourTile(direction))
                getNeighbourTile(direction).borderFlags |= (1 << direction.opposite().ordinal());
        } else {
            borderFlags &= (~(1 << direction.ordinal()));
            if (hasNeighbourTile(direction))
                getNeighbourTile(direction).borderFlags &= (~(1 << direction.opposite().ordinal()));
        }
    }

    TileLocation getNeighbourTile(TileDirection direction) {
        return space.getTileAt(getX() + direction.xTranslation, getY() + direction.yTranslation);
    }

    boolean hasNeighbourTile(TileDirection direction) {
        return space.hasTileAt(getX() + direction.xTranslation, getY() + direction.yTranslation);
    }

    public int getBorderFlags() {
        return borderFlags;
    }

    public void setBorderFlags(int borderFlags) {
        this.borderFlags = borderFlags;
    }

    public void toggleBorder(TileDirection direction) {
        setBorder(direction, !hasBorder(direction));
    }

    public boolean covers(Coordinates2D coordinates) {
        return (int) coordinates.getX() == x
                && (int) coordinates.getY() == y;
    }
}
