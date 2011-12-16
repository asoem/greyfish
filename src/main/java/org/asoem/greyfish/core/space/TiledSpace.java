package org.asoem.greyfish.core.space;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import javolution.util.FastMap;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.utils.space.*;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementArray;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;

import static com.google.common.base.Functions.forMap;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.utils.space.Conversions.polarToCartesian;
import static org.asoem.greyfish.utils.space.ImmutableCoordinates2D.sum;

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

    private final Map<Movable, Object2D> objectLocation2DMap = FastMap.newInstance();

    private final TwoDimTree<Movable> twoDimTree = AsoemScalaTwoDimTree.newInstance();
    private boolean twoDimTreeOutdated = false;


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

    public boolean covers(Coordinates2D value) {
        checkNotNull(value);
        return value.getX() == 0 && value.getY() == 0  // 0,0 is always covered
                || hasTileAt((int) Math.floor(value.getX()), (int) Math.floor(value.getY()));
    }

    public boolean hasTileAt(int x, int y) {
        return x >= 0 && x < width &&
                y >= 0 && y < height;
    }

    public TileLocation getTileAt(TileLocation location) {
        return getTileAt(location.getX(), location.getY());
    }

    public TileLocation getTileAt(final int x, final int y) throws IndexOutOfBoundsException, IllegalArgumentException {
        Preconditions.checkPositionIndex(x, width);
        Preconditions.checkPositionIndex(y, height);
        return tileMatrix[x][y];
    }

    @Override
    public String toString() {
        return "Tiled Space: dim="+width+"x"+height+"; oc="+Iterables.size(getOccupants());
    }

    public Iterable<Object2D> getOccupants() {
        return objectLocation2DMap.values();
    }

    private void updateTopo() {
        twoDimTree.rebuild(objectLocation2DMap.keySet(), forMap(Maps.<Movable, Object2D, Coordinates2D>transformValues(objectLocation2DMap,new Function<Object2D, Coordinates2D>() {
            @Override
            public Coordinates2D apply(Object2D object2D) {
                return object2D.getCoordinates();
            }
        })));
    }

    public TileLocation getTileAt(Coordinates2D coordinates2D) throws IndexOutOfBoundsException, IllegalArgumentException {
        return getTileAt((int) coordinates2D.getX(), (int) coordinates2D.getY());
    }

    /**
     *
     * @param movable the object to check for validity of the move operation.
     * @return A {@code MovementPlan} which can be used to check if the move will succeed and to execute the movement
     * if it does so using {@link #executeMovement(org.asoem.greyfish.core.space.TiledSpace.MovementPlan)}
     * @throws IllegalArgumentException if the {@code object2D} is not managed by this {@code TiledSpace}
     */
    public MovementPlan planMovement(Movable movable) {
        checkNotNull(movable);
        Object2D currentCoordinates = objectLocation2DMap.get(movable);
        checkNotNull(currentCoordinates, "Given object is not managed by (has not yet been added to) this space: " + movable);
        final TileLocation originTile = getTileAt(currentCoordinates.getCoordinates());
        double angle = currentCoordinates.getOrientation() + movable.getRotation();
        Coordinates2D newCoordinates = sum(currentCoordinates.getCoordinates(), polarToCartesian(angle, movable.getTranslation()));

        return new MovementPlan(
                movable,
                newCoordinates,
                ! covers(newCoordinates) || originTile.hasBorder(TileDirection.forTiles(getTileAt(currentCoordinates.getCoordinates()), getTileAt(newCoordinates))),
                angle);
    }

    /**
     * Execute the {@code plan}. If the plan will result in a collision, than subject of the {@code plan} will just get rotated, but not translated
     * @param plan the planed movement
     */
    public void executeMovement(MovementPlan plan) {
        checkNotNull(plan);
        if (!plan.willCollide()) {
            synchronized (this) {
                objectLocation2DMap.put(plan.movable, ImmutableObject2D.of(plan.coordinates2D, plan.orientation));
            }
        }
        else {
            // TODO: translate as far as we can
            synchronized (this) {
                Object2D old = getObject(plan.movable);
                assert old != null;
                objectLocation2DMap.put(plan.movable, ImmutableObject2D.of(old.getCoordinates(), plan.orientation));
            }
        }
        twoDimTreeOutdated = true;
    }

    public void moveObject(Movable object2d) {
        executeMovement(planMovement(object2d));
    }

    /**
     *
     *
     * @param coordinates the coordinates of the search point
     * @param range the radius of the circle around {@code coordinates}
     * @return all objects whose location in this space
     * intersects with the circle defined by {@code coordinates} and {@code range}
     */
    public Iterable<Movable> findObjects(Coordinates2D coordinates, double range) {
        if (twoDimTreeOutdated)
            updateTopo();
        return twoDimTree.findObjects(coordinates, range);
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

    public Iterable<Movable> getOccupants(final TileLocation location) {
        return Maps.filterValues(objectLocation2DMap, new Predicate<Object2D>() {
            @Override
            public boolean apply(Object2D coordinates2D) {
                return location.covers(coordinates2D.getCoordinates());
            }
        }).keySet();
    }

    public void addObject(Movable movable, Coordinates2D coordinates2D) {
        checkArgument(this.covers(checkNotNull(coordinates2D)));
        checkNotNull(movable);
        synchronized (this) {
            objectLocation2DMap.put(movable, ImmutableObject2D.of(coordinates2D,0));
            twoDimTreeOutdated = true;
        }
    }

    public void removeObject(Movable movable) {
        checkNotNull(movable);
        synchronized (this) {
            if(objectLocation2DMap.remove(movable) == null) {
                throw new IllegalArgumentException("Object not maintained by this space: " + movable);
            }
            twoDimTreeOutdated = true;
        }
    }

    @Nullable
    public Object2D getObject(Movable agent) {
        return objectLocation2DMap.get(agent);
    }

    public Coordinates2D getCoordinates(Movable agent) {
        return objectLocation2DMap.get(agent).getCoordinates();
    }

    public Iterable<Movable> findObjects(Movable agent, double radius) {
        return findObjects(getCoordinates(agent), radius);
    }

    public static class MovementPlan {
        private final Movable movable;
        private final Coordinates2D coordinates2D;
        private final boolean willSucceed;
        public final double orientation;

        private MovementPlan(Movable movable, Coordinates2D coordinates2D, boolean collision, double orientation) {
            this.willSucceed = collision;
            this.orientation = orientation;
            assert movable != null;
            assert coordinates2D != null;
            this.movable = movable;
            this.coordinates2D = coordinates2D;
        }

        public boolean willCollide() {
            return willSucceed;
        }
    }
}
