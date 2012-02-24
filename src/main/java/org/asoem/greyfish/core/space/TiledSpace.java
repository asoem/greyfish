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
import org.simpleframework.xml.ElementMap;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static com.google.common.base.Functions.forMap;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.utils.space.Conversions.polarToCartesian;
import static org.asoem.greyfish.utils.space.ImmutableLocatable2D.sum;

/**
 * @author christoph
 * This class is used to handle a 2D space implemented as a Matrix of Locations.
 */
public class TiledSpace implements Iterable<TileLocation> {

    @Attribute(name = "height")
    private final int height;

    @Attribute(name = "width")
    private final int width;

    @ElementMap(name = "spaceObjectMap")
    private final Map<Object, Object2D> spaceObjectMap = FastMap.newInstance();

    private final TileLocation[][] tileMatrix;

    private final TwoDimTree<Object> twoDimTree = AsoemScalaTwoDimTree.newInstance();

    private boolean twoDimTreeOutdated = false;

    public TiledSpace(TiledSpace pSpace) {
        this(checkNotNull(pSpace).getWidth(), pSpace.getHeight());
        setBorderedTiles(pSpace.getBorderedTiles());
    }

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

    @SimpleXMLConstructor
    private TiledSpace(@Attribute(name = "width") int width,
                       @Attribute(name = "height") int height,
                       @ElementMap(name = "spaceObjectMap") Map<Motion2D, Object2D> spaceObjectMap) {
        this(width, height);
        this.spaceObjectMap.putAll(spaceObjectMap);
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

    public boolean covers(Locatable2D value) {
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

    private void updateTopo() {
        twoDimTree.rebuild(spaceObjectMap.keySet(), forMap(spaceObjectMap));
    }

    public TileLocation getTileAt(Locatable2D locatable2D) throws IndexOutOfBoundsException, IllegalArgumentException {
        return getTileAt((int) locatable2D.getX(), (int) locatable2D.getY());
    }

    /**
     *
     * @param agent the object to check for validity of the move operation.
     * @param motion2D the requested motion
     * @return A {@code MovementPlan} which can be used to check if the move will succeed and to execute the movement
     * if it does so using {@link #executeMovement(org.asoem.greyfish.core.space.TiledSpace.MovementPlan)}
     * @throws IllegalArgumentException if the {@code object2D} is not managed by this {@code TiledSpace}
     */
    public MovementPlan planMovement(Object agent, Motion2D motion2D) {
        checkNotNull(agent);
        checkNotNull(motion2D);
        Object2D currentCoordinates = spaceObjectMap.get(agent);
        checkNotNull(currentCoordinates, "Given object is not managed by (has not yet been added to) this space: " + motion2D);
        final TileLocation originTile = getTileAt(currentCoordinates);
        double angle = currentCoordinates.getOrientationAngle() + motion2D.getRotation2D();
        Locatable2D newLocatable = sum(currentCoordinates, polarToCartesian(angle, motion2D.getTranslation()));

        return new MovementPlan(
                agent,
                newLocatable,
                ! covers(newLocatable) || originTile.hasBorder(TileDirection.forTiles(getTileAt(currentCoordinates), getTileAt(newLocatable))),
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
                spaceObjectMap.put(plan.object, ImmutableObject2D.of(plan.locatable2D, plan.orientation));
            }
        }
        else {
            // TODO: translate as far as we can
            synchronized (this) {
                Object2D old = getObject(plan.object);
                assert old != null;
                spaceObjectMap.put(plan.object, ImmutableObject2D.of(old, plan.orientation));
            }
        }
        twoDimTreeOutdated = true;
    }

    public void moveObject(Object object2d, Motion2D motion) {
        executeMovement(planMovement(object2d, motion));
    }

    /**
     *
     *
     * @param locatable the locatable of the search point
     * @param range the radius of the circle around {@code locatable}
     * @return evaluates objects whose location in this space
     * intersects with the circle defined by {@code locatable} and {@code range}
     */
    public Iterable<Object> findObjects(Locatable2D locatable, double range) {
        if (twoDimTreeOutdated)
            updateTopo();
        return twoDimTree.findObjects(locatable, range);
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

    public Iterable<Object> getOccupants() {
        return spaceObjectMap.keySet();
    }

    public Iterable<Object> getOccupants(final TileLocation tileLocation) {
        return getOccupants(Collections.singleton(tileLocation));
    }

    public Iterable<Object> getOccupants(Iterable<? extends TileLocation> tileLocations) {
        return Iterables.concat(Iterables.transform(tileLocations, new Function<TileLocation, Iterable<Object>>() {
            @Override
            public Iterable<Object> apply(@Nullable final TileLocation tileLocation) {
                return Maps.filterValues(spaceObjectMap, new Predicate<Object2D>() {
                    @Override
                    public boolean apply(Object2D coordinates2D) {
                        assert tileLocation != null;
                        return tileLocation.covers(coordinates2D);
                    }
                }).keySet();
            }
        }));
    }

    public void addObject(Object motion2D, Locatable2D locatable2D) {
        checkArgument(this.covers(checkNotNull(locatable2D)));
        checkNotNull(motion2D);
        synchronized (this) {
            spaceObjectMap.put(motion2D, ImmutableObject2D.of(locatable2D, 0));
            twoDimTreeOutdated = true;
        }
    }

    public boolean removeObject(Object motion2D) {
        checkNotNull(motion2D);
        synchronized (this) {
            if (spaceObjectMap.remove(motion2D) != null) {
                twoDimTreeOutdated = true;
                return true;
            }
            else
                return false;
        }
    }

    @Nullable
    public Object2D getObject(Object agent) {
        return spaceObjectMap.get(agent);
    }

    public Locatable2D getCoordinates(Object agent) {
        return spaceObjectMap.get(agent);
    }

    public Iterable<Object> findObjects(Object agent, double radius) {
        return findObjects(getCoordinates(agent), radius);
    }

    public static class MovementPlan {
        private final Object object;
        private final Locatable2D locatable2D;
        private final boolean willSucceed;
        public final double orientation;

        private MovementPlan(Object object, Locatable2D locatable2D, boolean collision, double orientation) {
            this.willSucceed = collision;
            this.orientation = orientation;
            assert object != null;
            assert locatable2D != null;
            this.object = object;
            this.locatable2D = locatable2D;
        }

        public boolean willCollide() {
            return willSucceed;
        }
    }
}
