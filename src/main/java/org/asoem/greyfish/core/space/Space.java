package org.asoem.greyfish.core.space;

import org.simpleframework.xml.Root;

@Root
public interface Space {

	public void removeAllOccupants();

	public Iterable<MovingObject2D> getOccupants();

	public void addOccupant(MovingObject2D object2d);

	public boolean removeOccupant(MovingObject2D object2d);

	public boolean canMove(MovingObject2D object2d, Location2D newLocation);

	public TileLocation getTileAt(Location2D componentOwner);
	
	/**
	 * @param <T>
	 * @param point 
	 * @param range
	 * @return all found objects in the given range around {@code point} in this space
	 */
	Iterable<MovingObject2D> findNeighbours(Location2D p,
			double range);

    public boolean covers(Location2D value);
}
