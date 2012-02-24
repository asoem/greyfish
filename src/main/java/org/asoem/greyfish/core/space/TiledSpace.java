package org.asoem.greyfish.core.space;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import javolution.util.FastList;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.utils.space.*;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementArray;
import org.simpleframework.xml.ElementList;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.utils.space.Conversions.polarToCartesian;
import static org.asoem.greyfish.utils.space.ImmutableLocation2D.sum;

/**
 * @author christoph
 * This class is used to handle a 2D space implemented as a Matrix of Locations.
 */
public class TiledSpace implements Iterable<TileLocation> {

    @Attribute(name = "height")
    private final int height;

    @Attribute(name = "width")
    private final int width;

    @ElementList(name = "projectables")
    private final List<Projectable<Object2D>> projectables = FastList.newInstance();

    private final TileLocation[][] tileMatrix;

    private final TwoDimTree<Projectable<Object2D>> twoDimTree = AsoemScalaTwoDimTree.newInstance();

    private boolean twoDimTreeOutdated = false;

    public TiledSpace(TiledSpace pSpace) {
        this(checkNotNull(pSpace).getWidth(), pSpace.getHeight());
        setBorderedTiles(pSpace.getBorderedTiles());
    }

    public TiledSpace(int width,
                      int height) {
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

    public TiledSpace(int width, int height, TileLocation[] borderedTiles) {
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
        
        setBorderedTiles(borderedTiles);
    }

    @SimpleXMLConstructor
    private TiledSpace(@Attribute(name = "width") int width,
                       @Attribute(name = "height") int height,
                       @ElementList(name = "projectables") List<Projectable<Object2D>> projectables) {
        this(width, height);
        this.projectables.addAll(projectables);
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
        twoDimTree.rebuild(projectables, new Function<Projectable<Object2D>, Location2D>() {
            @Override
            public Location2D apply(@Nullable Projectable<Object2D> o) {
                assert o != null;
                return o.getProjection();
            }
        });
    }

    public TileLocation getTileAt(Location2D locatable2D) throws IndexOutOfBoundsException, IllegalArgumentException {
        return getTileAt((int) locatable2D.getX(), (int) locatable2D.getY());
    }

    /**
     *
     *
     * @param agent the projectable to check for validity of the move operation.
     * @param motion2D the requested motion
     * @return A {@code MovementPlan} which can be used to check if the move will succeed and to execute the movement
     * if it does so using {@link #executeMovement(org.asoem.greyfish.core.space.TiledSpace.MovementPlan)}
     * @throws IllegalArgumentException if the {@code object2D} is not managed by this {@code TiledSpace}
     */
    public MovementPlan planMovement(Projectable<Object2D> agent, Motion2D motion2D) {
        checkNotNull(agent);
        checkNotNull(motion2D);
        Object2D agentLocation = agent.getProjection();
        checkNotNull(agentLocation, "Given projectable is not managed by (has not yet been added to) this space: " + motion2D);
        final TileLocation originTile = getTileAt(agentLocation);
        double newOrientation = agentLocation.getOrientationAngle() + motion2D.getRotation2D();
        final ImmutableLocation2D newLocation = sum(agentLocation, polarToCartesian(newOrientation, motion2D.getTranslation()));
        Object2D newLocatable = ImmutableObject2D.of(newLocation, newOrientation);

        return new MovementPlan(
                agent,
                newLocatable,
                ! covers(newLocatable) || originTile.hasBorder(TileDirection.forTiles(getTileAt(agentLocation), getTileAt(newLocatable))));
    }

    /**
     * Execute the {@code plan}. If the plan will result in a collision, than subject of the {@code plan} will just get rotated, but not translated
     * @param plan the planed movement
     */
    public void executeMovement(MovementPlan plan) {
        checkNotNull(plan);
        if (!plan.willCollide()) {
            synchronized (this) {
                plan.projectable.setProjection(plan.projection);
            }
        }
        else {
            // TODO: translate as far as we can
            synchronized (this) {
                assert projectables.contains(plan.projectable);
                plan.projectable.setProjection(plan.projection);
            }
        }
        twoDimTreeOutdated = true;
    }

    public void moveObject(Projectable<Object2D> object2d, Motion2D motion) {
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
    public Iterable<Projectable<Object2D>> findObjects(Location2D locatable, double range) {
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

    public Iterable<Projectable<Object2D>> getOccupants() {
        return projectables;
    }

    public Iterable<Projectable<Object2D>> getOccupants(final TileLocation tileLocation) {
        return getOccupants(Collections.singleton(tileLocation));
    }

    public Iterable<Projectable<Object2D>> getOccupants(Iterable<? extends TileLocation> tileLocations) {
        return Iterables.concat(Iterables.transform(tileLocations, new Function<TileLocation, Iterable<Projectable<Object2D>>>() {
            @Override
            public Iterable<Projectable<Object2D>> apply(@Nullable final TileLocation tileLocation) {
                return Iterables.filter(projectables, new Predicate<Projectable<Object2D>>() {
                    @Override
                    public boolean apply(Projectable<Object2D> coordinates2D) {
                        assert tileLocation != null;
                        return tileLocation.covers(coordinates2D.getProjection());
                    }
                });
            }
        }));
    }

    public void addObject(Projectable<Object2D> projectable, Object2D projection) {
        checkArgument(this.covers(checkNotNull(projection)));
        checkNotNull(projectable);
        synchronized (this) {
            projectables.add(projectable);
            projectable.setProjection(projection);
            twoDimTreeOutdated = true;
        }
    }

    public boolean removeObject(Projectable<Object2D> motion2D) {
        checkNotNull(motion2D);
        synchronized (this) {
            if (projectables.remove(motion2D)) {
                twoDimTreeOutdated = true;
                return true;
            }
            else
                return false;
        }
    }

    public Iterable<Projectable<Object2D>> getNeighbours(Projectable<Object2D> agent, double radius) {
        return Iterables.filter(findObjects(agent.getProjection(), radius), Predicates.not(Predicates.<Projectable<? extends Object2D>>equalTo(agent)));
    }

    /**
     * Create a new {@code TiledSpace} which has the same dimensions and the same borders as the given {@code space},
     * but with no objects.
     * @param space The space to copy the information from
     * @return a new space
     */
    public static TiledSpace createEmptySpace(TiledSpace space) {
        return new TiledSpace(space.getWidth(), space.getHeight(), space.getBorderedTiles());
    }


    public static class MovementPlan {
        private final Projectable<Object2D> projectable;
        private final Object2D projection;
        private final boolean willSucceed;

        private MovementPlan(Projectable<Object2D> projectable, Object2D projection, boolean collision) {
            this.willSucceed = collision;
            assert projectable != null;
            assert projection != null;
            this.projectable = projectable;
            this.projection = projection;
        }

        public boolean willCollide() {
            return willSucceed;
        }
    }
}
