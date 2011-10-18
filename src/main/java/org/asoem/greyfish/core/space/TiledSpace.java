package org.asoem.greyfish.core.space;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.utils.PolarPoint;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementArray;

import javax.annotation.Nullable;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author christoph
 * This class is used to handle a 2D space implemented as a Matrix of Locations.
 */
public class TiledSpace implements Iterable<TileLocation> {

    @Attribute(name="height")
    private final int height;

    @Attribute(name="width")
    private final int width;

    private final TileLocation[][] tileMatrix;

    private final KDTree<MovingObject2D> kdTree = AsoemScalaKDTree.newInstance();

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

    public static TiledSpace ofSize(int width, int height) {
        return new TiledSpace(width, height);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public boolean covers(Location2D value) {
        return covers(value.getCoordinates());
    }

    public boolean covers(Coordinates2D value) {
        checkNotNull(value);
        return value.getX() == 0 && value.getY() == 0  // 0,0 is always covered
                || hasTileAt((int) Math.floor(value.getX()), (int) Math.floor(value.getY()));
    }

    public boolean hasTileAt(int x, int y) {
        return x >= 0 && x < width &&
                y >= 0 && y < height;
    }

    public TileLocation getTileAt(Location2D location) {
        return getTileAt(location.getCoordinates());
    }

    public TileLocation getTileAt(TileLocation location) {
        return getTileAt(location.getX(), location.getY());
    }

    public TileLocation getTileAt(final int x, final int y) throws IndexOutOfBoundsException, IllegalArgumentException {
        Preconditions.checkPositionIndex(x, width);
        Preconditions.checkPositionIndex(y, height);
        return tileMatrix[x][y];
    }

    /*
    public void removeAllOccupants() {
        for (TileLocation location : this) {
            location.removeAllOccupants();
        }
        nOccupants = 0;
        dirty = true;
    }
    */

    @Override
    public String toString() {
        return "Tiled Space: dim="+width+"x"+height+"; oc="+Iterables.size(getOccupants());
    }

    /*
    public void addOccupant(MovingObject2D object2d) {
        checkNotNull(object2d);

        TileLocation loc = getTileAt(object2d);
        loc.addOccupant(object2d);
        ++nOccupants;
        dirty = true;
    }

    /*
    public boolean removeOccupant(MovingObject2D individual) {
        if (getTileAt(individual).removeOccupant(individual)) {
            --nOccupants;
            dirty = true;
            return true;
        }
        return false;
    }
    */

    public Iterable<MovingObject2D> getOccupants() {
        return kdTree;
    }

    public void updateTopo(Iterable<? extends MovingObject2D> objects) {
        kdTree.rebuild(objects);
    }

    public TileLocation getTileAt(Coordinates2D coordinates2D) throws IndexOutOfBoundsException, IllegalArgumentException {
        return getTileAt((int) coordinates2D.getX(), (int) coordinates2D.getY());
    }

    public boolean canMove(MovingObject2D origin, Coordinates2D destination) {
        if (!covers(origin)) {
            throw new IllegalArgumentException(String.format("No TileLocation for origin '%s' in %s", origin.getCoordinates(), this));
        }

        final TileLocation originTile = getTileAt(origin);

        if (covers(destination)) {
            final TileLocation destinationTile = getTileAt(destination);
            return originTile.hasReachableNeighbour(destinationTile);
        }
        return false;
    }

    public void moveObject(MovingObject2D object2d, Coordinates2D newCoordinates) {
        if (canMove(object2d, newCoordinates)) {
            final TileLocation loc = getTileAt(object2d);
            final TileLocation new_loc = getTileAt(newCoordinates);

            /*
            boolean result = loc.removeOccupant(object2d);
            assert(result);
            new_loc.addOccupant(object2d);
            */

            object2d.setAnchorPoint(newCoordinates);
        }
    }

    public boolean checkForBorderCollision(Coordinates2D origin, PolarPoint motionVector) {
        checkArgument(this.covers(checkNotNull(origin)));
        checkNotNull(motionVector);

        final Coordinates2D coordinatesAfterMove = ImmutableCoordinates2D.at(origin, motionVector.toCartesian());
        return !covers(coordinatesAfterMove) || getTileAt(origin).hasBorder(getTileAt(coordinatesAfterMove));

    }

    /**
     * @param coordinates the search point
     * @param range the radius of the circle around {@code coordinates}
     * @return all objects whose anchor point ({@link org.asoem.greyfish.core.space.MovingObject2D#getCoordinates()})
     * intersects with the circle defined by {@code coordinates} and {@code range}
     */
    public Iterable<MovingObject2D> findObjects(Coordinates2D coordinates, double range) {
        return kdTree.findObjects(coordinates, range);
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

    public Iterable<MovingObject2D> getOccupants(final TileLocation location) {
        return Iterables.filter(getOccupants(), new Predicate<MovingObject2D>() {
            @Override
            public boolean apply(@Nullable MovingObject2D movingObject2D) {
                assert movingObject2D != null;
                return location.covers(movingObject2D.getCoordinates());
            }
        });
    }
}
