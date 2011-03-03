package org.asoem.greyfish.core.space;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import javolution.util.FastList;
import org.asoem.greyfish.core.space.TiledSpace.Direction;
import org.simpleframework.xml.Attribute;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class TileLocation {

    @Attribute
    private final int x;

    @Attribute
    private final int y;

    final public static int BORDER_NORTH = 1;
    final public static int BORDER_WEST = 2;
    final public static int BORDER_SOUTH = 4;
    final public static int BORDER_EAST = 8;
    int borderFlags = 0;

    final List<MovingObject2D> occupants = FastList.newInstance();
    private Map<Direction, TileLocation> adjacents = ImmutableMap.of();
    private Map<Direction, TileLocation> reachables = ImmutableMap.of();

    TileLocation(TiledSpace space, int x, int y) {
        this.x = x;
        this.y = y;
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
        return "["+String.valueOf(x)+","+String.valueOf(y)+"]";
    }

    public boolean hasBorder(Direction direction) {
        return hasBorder(direction.borderCheck);
    }

    public boolean hasBorder(int orientation) {
        return (borderFlags & orientation) != 0;
    }

    public Iterable<MovingObject2D> getOccupants() {
        return occupants;
    }

    public Collection<TileLocation> getAdjacents() {
        return adjacents.values();
    }

    public void setAdjacents(Map<Direction, TileLocation> adjacents) {
        Preconditions.checkNotNull(adjacents);
        this.adjacents = ImmutableMap.copyOf(adjacents);
    }

    public Collection<TileLocation> getReachables() {
        return reachables.values();
    }

    public void setReachables(Map<Direction, TileLocation> reachables) {
        Preconditions.checkNotNull(reachables);
        this.reachables = ImmutableMap.copyOf(reachables);
    }

    /**
     * @param l2 TileLocation 2
     * @return {@code true} if {@code l2} is adjacent to and reachable from this
     */
    public boolean hasReachableNeighbour(TileLocation l2) {
        checkArgument(adjacents.containsValue(checkNotNull(l2)));
        return ! getReachables().contains(l2);
    }
}
