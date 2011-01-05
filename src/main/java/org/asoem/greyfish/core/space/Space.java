package org.asoem.sico.core.space;

import org.simpleframework.xml.Root;

@Root
public interface Space {

	public void removeAllOccupants();

	public Iterable<Object2DInterface> getOccupants();

	public void addOccupant(Object2DInterface object2d);

	public boolean removeOccupant(Object2DInterface object2d);

	public boolean canMove(Object2DInterface object2d, Location2DInterface newLocation);

	public TileLocation getLocation(Location2DInterface componentOwner);
	
	/**
	 * @param <T>
	 * @param point 
	 * @param range
	 * @return all found objects in the given range around {@code point} in this space
	 */
	Iterable<Object2DInterface> findNeighbours(Location2DInterface p,
			double range);
}
