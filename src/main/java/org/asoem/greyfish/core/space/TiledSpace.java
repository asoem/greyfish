package org.asoem.greyfish.core.space;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.utils.PolarPoint;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementArray;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author christoph
 * This class is used to handle a 2D space implemented as a Matrix of Locations.
 */
public class TiledSpace implements Space, Iterable<TileLocation> {

    @Attribute(name="height")
    private final int height;

    @Attribute(name="width")
    private final int width;

    private final TileLocation[][] tileMatrix;

    private int nOccupants;

    private final KDTree<MovingObject2D> kdtree = AsoemScalaKDTree.newInstance();

    public TiledSpace(TiledSpace pSpace) {
        this(checkNotNull(pSpace).getWidth(), pSpace.getHeight());
        setBorderedTiles(pSpace.getBorderedTiles());
    }

    @SimpleXMLConstructor
    public TiledSpace(@Attribute(name = "width") int width,
                       @Attribute(name = "height") int height) {
        Preconditions.checkArgument(width >= 0);
        Preconditions.checkArgument(height >= 0);

        this.width = width;
        this.height = height;

        this.tileMatrix = new TileLocation[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                this.tileMatrix[i][j] = new TileLocation(this, i, j);
            }
        }
    }

    @ElementArray(name = "tiles", entry = "tile", required = false)
    private TileLocation[] getBorderedTiles() {
        return Iterables.toArray(Iterables.filter(this, new Predicate<TileLocation>() {
            @Override
            public boolean apply(TileLocation tileLocation) {
                return checkNotNull(tileLocation).getBorderFlags() != 0;
            }
        }), TileLocation.class);
    }

    @ElementArray(name = "tiles", entry = "tile", required = false)
    private void setBorderedTiles(TileLocation[] tiles) {
        if (tiles != null) {
            for (TileLocation location : tiles)
                getTileAt(location).setBorderFlags(location.getBorderFlags());
        }
    }

    public static TiledSpace copyOf(TiledSpace space) {
        return new TiledSpace(space);
    }

    public static TiledSpace newInstance(int width, int height) {
        return new TiledSpace(width, height);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public boolean covers(Location2D value) {
        return hasTileAt((int) Math.floor(value.getX()), (int) Math.floor(value.getY()));
    }

    public boolean hasTileAt(int x, int y) {
        return x >= 0 && x < width &&
                y >= 0 && y < height;
    }

    private TileLocation getTileAt(TileLocation location) {
        return getTileAt(location.getX(), location.getY());
    }

    public TileLocation getTileAt(final int x, final int y) throws IndexOutOfBoundsException, IllegalArgumentException {
        Preconditions.checkPositionIndex(x, width);
        Preconditions.checkPositionIndex(y, height);
        return tileMatrix[x][y];
    }

    @Override
    public void removeAllOccupants() {
        for (TileLocation location : this) {
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

        TileLocation loc = getTileAt(object2d);
        loc.addOccupant(object2d);
        ++nOccupants;
    }

    @Override
    public boolean removeOccupant(MovingObject2D individual) {
        if (getTileAt(individual).removeOccupant(individual)) {
            --nOccupants;
            return true;
        }
        return false;
    }

    @Override
    public Iterable<MovingObject2D> getOccupants() {
        List<Iterable<MovingObject2D>> iterables = Lists.newArrayList();
        for (TileLocation location : this) {
            iterables.add(location.getOccupants());
        }
        return Iterables.concat(iterables);
    }

    public void updateTopo() {
        kdtree.rebuild(getOccupants());
    }

    @Override
    public TileLocation getTileAt(Location2D componentOwner) throws IndexOutOfBoundsException, IllegalArgumentException {
        return getTileAt((int) componentOwner.getX(), (int) componentOwner.getY());
    }

    @Override
    public boolean canMove(MovingObject2D origin, Location2D destination) {
        if (!covers(origin)) {
            throw new IllegalArgumentException(String.format("No TileLocation for origin '%s' in %s", origin.getAnchorPoint(), this));
        }

        final TileLocation originTile = getTileAt(origin);

        if (covers(destination)) {
            final TileLocation destinationTile = getTileAt(destination);
            return originTile.hasReachableNeighbour(destinationTile);
        }
        return false;
    }

    public void moveObject(MovingObject2D object2d, Location2D newLocation) {
        if (canMove(object2d, newLocation)) {
            final TileLocation loc = getTileAt(object2d);
            final TileLocation new_loc = getTileAt(newLocation);

            boolean result = loc.removeOccupant(object2d);
            assert(result);
            new_loc.addOccupant(object2d);

            object2d.setAnchorPoint(newLocation);
        }
    }

    public boolean checkForBorderCollision(Location2D l2d, PolarPoint motionVector) {
        checkArgument(this.covers(checkNotNull(l2d)));
        checkNotNull(motionVector);

        final Location2D locationAfterMove = ImmutableLocation2D.at(l2d, motionVector.toCartesian());
        return !covers(locationAfterMove) || getTileAt(l2d).hasBorder(getTileAt(locationAfterMove));

    }

    /* (non-Javadoc)
      * @see org.asoem.greyfish.core.space.Space#findNeighbours(org.asoem.greyfish.core.space.MutableLocation2D, double, java.lang.Class)
      */
    @Override
    public Iterable<MovingObject2D> findNeighbours(Location2D p, double range) {
        return kdtree.findNeighbours(p, range);
    }

    @Override
    public Iterator<TileLocation> iterator() {
        return Iterators.concat(Iterators.transform(Iterators.forArray(tileMatrix), new Function<TileLocation[], Iterator<TileLocation>>() {
            @Override
            public Iterator<TileLocation> apply(@Nullable TileLocation[] tileLocations) {
                return Iterators.forArray(tileLocations);
            }
        }));
    }
}
