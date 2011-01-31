package org.asoem.greyfish.core.space;

import javolution.util.FastList;
import org.asoem.greyfish.core.space.TiledSpace.Direction;
import org.simpleframework.xml.Attribute;

import java.util.List;

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
	
	final List<Object2DInterface> occupants = new FastList<Object2DInterface>();
	TileLocation[] adjacents;
	TileLocation[] reachables;
	
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
		if (x != other.x)
			return false;
		else if (y != other.y)
			return false;
		else
            return true;
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

	public Iterable<Object2DInterface> getOccupants() {
		return occupants;
	}
}
