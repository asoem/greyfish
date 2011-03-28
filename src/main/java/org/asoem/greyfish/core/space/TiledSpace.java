package org.asoem.greyfish.core.space;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.utils.PolarPoint;
import org.simpleframework.xml.Attribute;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.core.io.GreyfishLogger.GUI_LOGGER;

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

    @Attribute(name="height")
    private final int height;

    @Attribute(name="width")
    private final int width;

    private final TileLocation[][] tileMatrix;

    private int nOccupants;

    private final KDTree<MovingObject2D> kdtree = AsoemScalaKDTree.newInstance();

    @Override
    public boolean covers(Location2D value) {
        return value.getX() >= 0 && value.getX() <= width
                && value.getY() >= 0 && value.getY() <= height;
    }

    public static TiledSpace copyOf(TiledSpace space) {
        return new TiledSpace(space);
    }

    public boolean hasLocationAt(int x, int y) {
        return x >= 0 && y < width && y >= 0 && y < height;
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
        // CAVE! Order matters for the opposite() function

        final int xTranslation;
        final int yTranslation;
        final int borderCheck;

        private Direction(int yTranslation, int xTranslation, int borderCheck) {
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

    public TiledSpace(TiledSpace pSpace) {
        this(pSpace.getWidth(), pSpace.getHeight());
        for (TileLocation location : pSpace.tilesIterable) {
            getLocationAt(location).setBorderFlags(location.getBorderFlags());
        }
    }

    private TileLocation getLocationAt(TileLocation location) {
        return getLocationAt(location.getX(), location.getY());
    }

    public static TiledSpace newInstance(int width, int height) {
        return new TiledSpace(width, height);
    }

    private TiledSpace(@Attribute(name = "width") int width, @Attribute(name = "height") int height) {
        Preconditions.checkArgument(width >= 0);
        Preconditions.checkArgument(height >= 0);

        this.width = width;
        this.height = height;
        this.tileMatrix = new TileLocation[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                tileMatrix[i][j] = new TileLocation(this, i, j);
            }
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public TileLocation getLocationAt(final int x, final int y) throws IndexOutOfBoundsException, IllegalArgumentException {
        Preconditions.checkPositionIndex(x, width);
        Preconditions.checkPositionIndex(y, height);
        return tileMatrix[x][y];
    }

    @Override
    public void removeAllOccupants() {
        for (TileLocation location : tilesIterable) {
            location.removeAllOccupants();
        }
    }

    @Override
    public String toString() {
        return "Tiled Space: dim="+width+"x"+height+"; oc="+nOccupants;
    }

    @Override
    public void addOccupant(MovingObject2D object2d) {
        checkNotNull(object2d);

        TileLocation loc = getLocation(object2d);
        loc.addOccupant(object2d);
        ++nOccupants;
    }

    @Override
    public boolean removeOccupant(MovingObject2D individual) {
        if (getLocation(individual).removeOccupant(individual)) {
            --nOccupants;
            return true;
        }
        return false;
    }

    @Override
    public Iterable<MovingObject2D> getOccupants() {
        List<Iterable<MovingObject2D>> iterables = Lists.newArrayList();
        for (TileLocation location : tilesIterable) {
            iterables.add(location.getOccupants());
        }
        return Iterables.concat(iterables);
    }

    public void updateTopo() {
        kdtree.rebuild(getOccupants());
    }

    @Override
    public TileLocation getLocation(Location2D componentOwner) throws IndexOutOfBoundsException, IllegalArgumentException {
        return getLocationAt((int)componentOwner.getX(), (int)componentOwner.getY());
    }

    @Override
    public boolean canMove(MovingObject2D origin, Location2D destination) {
        if (!covers(origin)) {
            if (GUI_LOGGER.isDebugEnabled())
                GUI_LOGGER.debug("No TileLocation for " + origin.getAnchorPoint() + " in " + this);
            return false;
        }

        final TileLocation originTile = getLocation(origin);

        if (covers(destination)) {
            TileLocation destinationTile = getLocation(destination);
            return originTile.hasReachableNeighbour(destinationTile);
        }
        return false;
    }

    public void moveObject(MovingObject2D object2d, Location2D newLocation) {
        if (canMove(object2d, newLocation)) {
            TileLocation loc = getLocation(object2d);
            boolean result = loc.removeOccupant(object2d);
            assert(result);

            TileLocation new_loc = getLocation(newLocation);
            new_loc.addOccupant(object2d);

            object2d.setAnchorPoint(newLocation);
        }
    }

    public boolean checkForBorderCollision(Location2D l2d, PolarPoint motionVector) {
        checkArgument(this.covers(checkNotNull(l2d)));
        checkNotNull(motionVector);

        final Location2D locationAfterMove = ImmutableLocation2D.at(l2d, motionVector.toCartesian());
        return !covers(locationAfterMove) || getLocation(l2d).hasBorder(getLocation(locationAfterMove));

    }

    /* (non-Javadoc)
      * @see org.asoem.greyfish.core.space.Space#findNeighbours(org.asoem.greyfish.core.space.MutableLocation2D, double, java.lang.Class)
      */
    @Override
    public Iterable<MovingObject2D> findNeighbours(Location2D p, double range) {
        return kdtree.findNeighbours(p, range);
    }
}
