package org.asoem.sico.core.space;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.asoem.sico.core.io.GreyfishLogger;
import org.asoem.sico.utils.RandomUtils;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.core.Commit;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * @author christoph
 * This class is used to handle a 2D space implemented as a Matrix of Locations.
 */
public class TiledSpace implements Space {

	private final Iterable<TileLocation> tilesIterable = new Iterable<TileLocation>() {
		@Override
		public Iterator<TileLocation> iterator() {
			return new Iterator<TileLocation>() {

				private int x = -1;
				private int y = 0;

				@Override
				public boolean hasNext() {
					return x < width -1 || y < height -1;
				}

				@Override
				public TileLocation next() throws NoSuchElementException {
					if ( x == width -1) {
						x = -1;
						++y;
					}
					++x;
					try {
						return getLocationAt(x, y);
					}
					catch (Exception e) {
						throw new NoSuchElementException();
					}
				}

				@Override
				public void remove() throws UnsupportedOperationException {
					throw new UnsupportedOperationException();
				}
			};
		}	
	};

	private final Iterable<Object2DInterface> occupantsIterable = new Iterable<Object2DInterface>() {
		@Override
		public Iterator<Object2DInterface> iterator() {
			return new Iterator<Object2DInterface>() {

				private Iterator<TileLocation> locIterator = tilesIterable.iterator();
				private Iterator<Object2DInterface> occIterator = null;

				@Override
				public boolean hasNext() {
					if (occIterator != null && occIterator.hasNext())
						return true;
					else {
						if (locIterator.hasNext()) {
							occIterator = locIterator.next().occupants.iterator();
							return hasNext();
						}
					}
					return false;
				}

				@Override
				public Object2DInterface next() throws NoSuchElementException {
					if ( ! hasNext() )
						throw new NoSuchElementException();
					return occIterator.next();
				}

				@Override
				public void remove() {
					occIterator.remove();
				}
			};
		}	
	};

	@Attribute(name="height")
	private int height;

	@Attribute(name="width")
	private int width;

	private TileLocation[][] tileMatrix;

	private static final int DIMENSIONS = 3;

	/* reconstructed each step */
	//private KDTree<Object2DInterface> kdTree = new KDTree<Object2DInterface>(DIMENSIONS);

	private int nOccupants;

	private final Object2DListener listener = new Object2DListener() {

		@Override
		public void hasMoved(Object2D object2d, Location2D oldLocation) {

		}
	};

	public int countOwnersAt(int i, int j) {
		return tileMatrix[i][j].occupants.size();
	}

	@SuppressWarnings("unused")
	@Commit
	private final void commit() {
		initFields(width, height);
	}

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
		// CAVE! Order matters for the reverse() function

		private int xTranslation;
		private int yTranslation;
		int borderCheck;

		private Direction(int yTranslation, int xTranslation, int borderCheck) {
			this.xTranslation = xTranslation;
			this.yTranslation = yTranslation;
			this.borderCheck = borderCheck;
		}

		public Direction reverse() {
			if (this == CENTER)
				return CENTER;
			if ((this.ordinal() & 1) != 0) // odd
				return Direction.values()[this.ordinal()+1];
			else
				return Direction.values()[this.ordinal()-1];
		}
	};

	public TiledSpace() {
		initFields(0,0);
	}

	public TiledSpace(int width, int height) {
		initFields(width, height);
	}

	public TiledSpace(TiledSpace pSpace) {
		initFields(pSpace.getWidth(), pSpace.getHeight());
	}

	private void initFields(int width, int height) {
		if(width<0 || height<0)
			throw new IllegalArgumentException();

		this.width = width;
		this.height = height;
		this.tileMatrix = new TileLocation[width][height];

		//createRandomBorders();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				TileLocation location = new TileLocation(this, i, j);
				tileMatrix[i][j] = location;
				if (i == 0) {
					location.borderFlags |= TileLocation.BORDER_WEST;
					if (hasDestination(location, Direction.WEST))
						getDestination(location, Direction.WEST).borderFlags |= TileLocation.BORDER_EAST;
				}
				if (i == width) {
					location.borderFlags |= TileLocation.BORDER_EAST;
					if (hasDestination(location, Direction.EAST))
						getDestination(location, Direction.EAST).borderFlags |= TileLocation.BORDER_WEST;
				}
				if (j == 0) {
					location.borderFlags |= TileLocation.BORDER_NORTH;
					if (hasDestination(location, Direction.NORTH))
						getDestination(location, Direction.NORTH).borderFlags |= TileLocation.BORDER_SOUTH;
				}
				if (j == height) {
					location.borderFlags |= TileLocation.BORDER_SOUTH;
					if (hasDestination(location, Direction.SOUTH))
						getDestination(location, Direction.SOUTH).borderFlags |= TileLocation.BORDER_NORTH;
				}
			}
		}

		for (TileLocation location : tilesIterable) {

			final ArrayList<TileLocation> adjacentsList = new ArrayList<TileLocation>();
			final ArrayList<TileLocation> reachablesList = new ArrayList<TileLocation>();

			for (Direction direction : Direction.values()) {
				TileLocation adjacentLocation = getAdjacentLocation(location, direction);
				if (adjacentLocation != null) {
					adjacentsList.add(adjacentLocation);
					if (borderCheck(location, direction)
							&& borderCheck(adjacentLocation, direction.reverse())) {
						reachablesList.add(adjacentLocation);
					}
				}
			}

			location.adjacents = (TileLocation[]) adjacentsList
				.toArray(new TileLocation[adjacentsList.size()]);
			location.reachables = (TileLocation[]) reachablesList
				.toArray(new TileLocation[adjacentsList.size()]);
		}
	}

	@SuppressWarnings("unused")
	private final void createRandomBorders() {
		for (TileLocation location : tilesIterable) {
			if (RandomUtils.nextFloat() < 0.2) {
				location.borderFlags |= TileLocation.BORDER_WEST;
				if (hasDestination(location, Direction.WEST))
					getDestination(location, Direction.WEST).borderFlags |= TileLocation.BORDER_EAST;
			}
			if (RandomUtils.nextFloat() < 0.2) {
				location.borderFlags |= TileLocation.BORDER_EAST;
				if (hasDestination(location, Direction.EAST))
					getDestination(location, Direction.EAST).borderFlags |= TileLocation.BORDER_WEST;
			}
			if (RandomUtils.nextFloat() < 0.2) {
				location.borderFlags |= TileLocation.BORDER_NORTH;
				if (hasDestination(location, Direction.NORTH))
					getDestination(location, Direction.NORTH).borderFlags |= TileLocation.BORDER_SOUTH;
			}
			if (RandomUtils.nextFloat() < 0.2) {
				location.borderFlags |= TileLocation.BORDER_SOUTH;
				if (hasDestination(location, Direction.SOUTH))
					getDestination(location, Direction.SOUTH).borderFlags |= TileLocation.BORDER_NORTH;
			}
		}
	}

	/**
	 * @param location
	 * @param direction
	 * @return the adjacent Location in the given <code>direction</code>
	 * to the passed <code>location</code>,
	 * which is <code>null</code> if <code>location</code> is at a border of the matrix.
	 */
	public TileLocation getAdjacentLocation(TileLocation location, Direction direction) {
		return getDestination(location, direction);
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public TileLocation getLocationAt(final int x, final int y) {
		Preconditions.checkPositionIndex(x, width);
		Preconditions.checkPositionIndex(y, height);
		return tileMatrix[x][y];
	}

	public TileLocation[] getAdjacentLocations(TileLocation location) {
		return ((TileLocation)location).adjacents;
	}

	public TileLocation[] getReachableLocations(TileLocation location) {
		return location.reachables;
	}

	@Override
	public void removeAllOccupants() {
		for (TileLocation location : tilesIterable) {
			location.occupants.clear();
		}
	}

	@Override
	public String toString() {
		return "Tiled Space: dim="+width+"x"+height+"; oc="+nOccupants;
	}

	private int ownerCount() {
		return nOccupants;
	}

	@Override
	public Iterable<Object2DInterface> getOccupants() {
		return occupantsIterable;
	}

	public TileLocation getRandomLocation() {
		return getLocationAt(RandomUtils.nextInt(width), RandomUtils.nextInt(height));
	}

	public void updateTopo() {
		/*
		kdTree = new KDTree<Object2DInterface>(DIMENSIONS);
		for (Object2DInterface individual : occupantsIterable) {
			try {
				final Location2D b = individual.getAnchorPoint();
				final double[] point = new double[] {b.getX(), b.getY(), RandomUtils.nextDouble(0.0, 0.000001)};
				kdTree.insert(point, individual);
			} catch (KeySizeException e) {
				throw new AssertionError();
			} catch (KeyDuplicateException e) {
				GreyfishLogger.warn("", e);
			}
		}
		//*/
	}

	private boolean hasLocation(Location2DInterface newLocation) {
		float x = newLocation.getX();
		float y = newLocation.getY();
		return (x >= 0 && x < width && y >= 0 && y < height);
	}

	@Override
	public TileLocation getLocation(Location2DInterface componentOwner) {
		return getLocationAt((int)componentOwner.getX(), (int)componentOwner.getY());
	}


	@Override
	public boolean removeOccupant(Object2DInterface individual) {
		if (getLocation(individual).occupants.remove(individual)) {
			--nOccupants;
			return true;
		}
		return false;
	}

	public Collection<Object2DInterface> getOccupants(TileLocation abstractLocation) {
		return abstractLocation.occupants;
	}

	public void addOccupant(Object2DInterface object2d) {
		Preconditions.checkNotNull(object2d);
		TileLocation loc = getLocation(object2d.getAnchorPoint());
		loc.occupants.add(object2d);
		++nOccupants;
		object2d.addListener(listener);
	}

	@Override
	public boolean canMove(Object2DInterface object2d, Location2DInterface newLocation) {
		TileLocation loc = getLocation(object2d);
		if (hasLocation(newLocation)) {
			TileLocation new_loc = getLocation(newLocation);

			if ( ! loc.equals(new_loc) ) {
				if (loc.getX() < new_loc.getX() && loc.hasBorder(Direction.EAST))
					return false;
				else if (loc.getX() > new_loc.getX() && loc.hasBorder(Direction.WEST))
					return false;
				if (loc.getY() < new_loc.getY() && loc.hasBorder(Direction.SOUTH))
					return false;
				else if (loc.getY() > new_loc.getY() && loc.hasBorder(Direction.NORTH))
					return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * @param object2d
	 * @param newLocation
	 * @return the location after the move which might be the old one due to constraints in this space
	 */
	public Location2DInterface moveObject(Object2DInterface object2d, Location2DInterface newLocation) {
		if (canMove(object2d, newLocation)) {
			TileLocation loc = getLocation(object2d);
			boolean result = loc.occupants.remove(object2d);
			assert(result);

			TileLocation new_loc = getLocation(newLocation);
			new_loc.occupants.add(object2d);

			object2d.setAnchorPoint(newLocation);

			return newLocation;
		}
		return object2d;
	}

	public void add(Object2DInterface object2d, Location2D location2d) {
		Preconditions.checkNotNull(object2d);
		Preconditions.checkNotNull(location2d);
		object2d.setAnchorPoint(location2d);
		addOccupant(object2d);
	}

	/**
	 * @param source
	 * @return the location in this direction given the source location, {@code null} if none exists
	 */
	public TileLocation getDestination(TileLocation source, Direction direction) {
		Preconditions.checkNotNull(source);
		return (direction == Direction.CENTER)
		? source
				: (hasDestination(source, direction))
				? this.getLocationAt(source.getX()+direction.xTranslation, source.getY()+direction.yTranslation)
						: null;
	}

	private boolean hasDestination(TileLocation source, Direction direction) {
		Preconditions.checkNotNull(source);
		return direction == Direction.CENTER
		|| source.getY() + direction.yTranslation >= 0
		&& source.getX() + direction.xTranslation >= 0
		&& source.getY() + direction.yTranslation < getHeight()
		&& source.getX() + direction.xTranslation < getWidth();
	}

	private static boolean borderCheck(TileLocation source, Direction direction) {
		assert (source != null);
		assert (direction != null);
		if (direction == Direction.CENTER)
			return true;
		if (source.hasBorder(direction.borderCheck))
			return false;
		else
			return true;
	}

	/* (non-Javadoc)
	 * @see org.asoem.sico.core.space.Space#findNeighbours(org.asoem.sico.core.space.Location2D, double, java.lang.Class)
	 */
	@Override
	public Iterable<Object2DInterface> findNeighbours(Location2DInterface p, double range) {

		Iterable<Object2DInterface> found = null;
			double[] point = new double[] {p.getX(), p.getY(), 0};

			long start = 0;
			if (GreyfishLogger.isTraceEnabled())
				start  = System.currentTimeMillis();

			//found = kdTree.nearestEuclidean(point, range);

			if (GreyfishLogger.isTraceEnabled()) {
				long end = System.currentTimeMillis();
				GreyfishLogger.trace(TiledSpace.class.getSimpleName() + "#findNeighbours: Found " + Iterables.size(found) + " Neighbours in " + (end - start) + "ms");
			}

		return (found == null) ? ImmutableList.<Object2DInterface>of() : found;
	}
}
