package org.asoem.greyfish.core.space;

import com.google.common.base.*;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import javolution.util.FastList;
import org.asoem.greyfish.core.simulation.Simulatable2D;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.utils.base.Builder;
import org.asoem.greyfish.utils.space.*;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementArray;
import org.simpleframework.xml.ElementList;

import javax.annotation.Nullable;
import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static javolution.lang.MathLib.TWO_PI;
import static org.asoem.greyfish.utils.space.Conversions.polarToCartesian;
import static org.asoem.greyfish.utils.space.Geometry2D.intersection;
import static org.asoem.greyfish.utils.space.ImmutableLocation2D.sum;

/**
 * @author christoph
 * This class is used to handle a 2D space implemented as a Matrix of Locations.
 */
public class TiledSpace<T extends Projectable<Object2D>> implements Iterable<TileLocation> {

    @Attribute(name = "height")
    private final int height;

    @Attribute(name = "width")
    private final int width;

    @ElementList(name = "projectables")
    private final List<T> projectables = FastList.newInstance();

    private final TileLocation[][] tileMatrix;

    private final TwoDimTree<Projectable<Object2D>> twoDimTree = AsoemScalaTwoDimTree.newInstance();

    private boolean twoDimTreeOutdated = false;

    public TiledSpace(TiledSpace pSpace) {
        this(checkNotNull(pSpace).getWidth(), pSpace.getHeight());
        setBorderedTiles(pSpace.getBorderedTiles());
    }

    public TiledSpace(int width,
                      int height) {
        this(width, height, new TileLocation[0]);
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
                       @ElementList(name = "projectables") List<T> projectables,
                       @ElementArray(name = "borderedTiles", entry = "tile", required = false) TileLocation[] borderedTiles) {
        this(width, height, borderedTiles);
        this.projectables.addAll(projectables);
    }

    /**
     * Constructs a new {@code TiledSpace} which has the same layout (size and walls) as {@code space}
     * and filled with objects after transforming them using the given {@code function}
     * @param space The template space
     * @param function A function to the transform the objects in {@code space} into this space
     */
    public TiledSpace(TiledSpace<T> space, Function<T, T> function) {
        this(space.getWidth(), space.getHeight(), space.getBorderedTiles());
        Iterables.addAll(projectables, Iterables.transform(space.getOccupants(), function));
    }

    public TiledSpace(TiledSpaceBuilder<T> builder) {
        this(builder.width, builder.height);
        for (TiledSpaceBuilder.TileBorderDefinition borderDefinition : builder.borderDefinitions) {
            getTileAt(borderDefinition.x, borderDefinition.y).setBorder(borderDefinition.direction, true);
        }
    }

    @ElementArray(name = "borderedTiles", entry = "tile", required = false)
    private TileLocation[] getBorderedTiles() {
        return Iterables.toArray(Iterables.filter(this, new Predicate<TileLocation>() {
            @Override
            public boolean apply(TileLocation tileLocation) {
                return checkNotNull(tileLocation).getBorderFlags() != 0;
            }
        }), TileLocation.class);
    }

    private void setBorderedTiles(TileLocation[] tiles) {
        if (tiles != null) {
            for (TileLocation location : tiles)
                getTileAt(location).setBorderFlags(location.getBorderFlags());
        }
    }

    public static TiledSpace copyOf(TiledSpace space) {
        return new TiledSpace(space);
    }

    public static <T extends Projectable<Object2D>> TiledSpace<T> ofSize(int width, int height) {
        return new TiledSpace<T>(width, height);
    }

    public static <T extends Projectable<Object2D>> TiledSpaceBuilder<T> builder(int width, int height) {
        return new TiledSpaceBuilder<T>(width, height);
    }
    
    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public boolean covers(Location2D location) {
        checkNotNull(location);
        return hasTileAt((int) Math.floor(location.getX()), (int) Math.floor(location.getY()));
    }

    public boolean hasTileAt(int x, int y) {
        return x >= 0 && x < width &&
                y >= 0 && y < height;
    }

    public TileLocation getTileAt(TileLocation location) {
        return getTileAt(location.getX(), location.getY());
    }

    public TileLocation getTileAt(final int x, final int y) {
        Preconditions.checkArgument(hasTileAt(x, y));
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

    public TileLocation getTileAt(Location2D location2D) throws IndexOutOfBoundsException, IllegalArgumentException {
        checkArgument(covers(location2D), "There is no tile for location [%s, %s]", location2D.getX(), location2D.getY());
        return getTileAt(location2D.getX(), location2D.getY());
    }

    private TileLocation getTileAt(double x, double y) {
        return getTileAt((int) Math.floor(x), (int) Math.floor(y));
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
    @SuppressWarnings("ConstantConditions")
    public MovementPlan<T> planMovement(T agent, Motion2D motion2D) {
        checkNotNull(agent);
        final Object2D currentProjection = agent.getProjection();
        checkNotNull(currentProjection, "Given projectable has no projection. Have you added it to this Space?", agent);
        checkNotNull(motion2D);
        checkArgument(Math.abs(motion2D.getTranslation())  <= 1, "Translations > 1 are not supported", motion2D.getTranslation());

        final double newOrientation = ((currentProjection.getOrientationAngle() + motion2D.getRotation2D()) % TWO_PI + TWO_PI) % TWO_PI;
        final double translation = motion2D.getTranslation();
        final ImmutableLocation2D newLocation = sum(currentProjection, polarToCartesian(newOrientation, translation));
        final Object2D newProjection = ImmutableObject2D.of(newLocation, newOrientation);

        return new MovementPlan<T>(
                agent,
                newProjection, maxTransition(currentProjection, newProjection));
    }

    /**
     * Get the location of the transition from {@code origin} to {@code destination} respecting collision with walls.
     * So, if there is no wall between {@code origin} and {@code destination} that this method returns {@code destination}.
     * Otherwise it returns the first {@code Location2D} at which the line from {@code origin} to {@code destination}
     * intersects with a wall of any crossing tile.
     * @param origin The origin of the transition
     * @param destination The destination of the transition
     * @return The point of the first collision, or {@code destination} if none occurs
     */
    public Location2D maxTransition(Location2D origin, Location2D destination) {
        return Optional.fromNullable(collision(getTileAt(origin),
                new Line2D.Double(origin.getX(), origin.getY(), destination.getX(), destination.getY()),
                new boolean[] {
                        destination.getY() < origin.getY(),
                        destination.getX() > origin.getX(),
                        destination.getY() > origin.getY(),
                        destination.getX() < origin.getX()
                })).or(destination);
    }

    @Nullable
    private static Location2D collision(@Nullable TileLocation location, Line2D line2D, boolean [] movementDirection) {
        assert movementDirection.length == 4;
        assert line2D != null;

        Location2D ret = null;
        
        if (location != null && line2D.intersects(location.getX(), location.getY(), 1, 1)) {
            if (movementDirection[0]) { // north
                final ImmutableLocation2D intersection = intersection(
                        location.getX(), location.getY(), location.getX() + 1, location.getY(),
                        line2D.getX1(), line2D.getY1(), line2D.getX2(), line2D.getY2());
                if (intersection != null) {
                    // maxTransition ? return maxTransition : maxTransition at adjacent tile?
                    ret = (location.hasBorder(TileDirection.NORTH)) ? intersection
                            : collision(location.getAdjacent(TileDirection.NORTH), line2D, movementDirection);
                }
            }

            if (ret == null && movementDirection[1]) { // east
                final ImmutableLocation2D intersection = intersection(
                        location.getX() + 1, location.getY(), location.getX() + 1, location.getY() + 1,
                        line2D.getX1(), line2D.getY1(), line2D.getX2(), line2D.getY2());
                if (intersection != null) {
                    // maxTransition ? return maxTransition : maxTransition at adjacent tile?
                    ret = (location.hasBorder(TileDirection.EAST)) ? intersection
                            : collision(location.getAdjacent(TileDirection.EAST), line2D, movementDirection);
                }
            }

            if (ret == null && movementDirection[2]) { // south
                final ImmutableLocation2D intersection = intersection(
                        location.getX() + 1, location.getY() + 1, location.getX(), location.getY() + 1,
                        line2D.getX1(), line2D.getY1(), line2D.getX2(), line2D.getY2());
                if (intersection != null) {
                    // maxTransition ? return maxTransition : maxTransition at adjacent tile?
                    ret = (location.hasBorder(TileDirection.SOUTH)) ? intersection
                            : collision(location.getAdjacent(TileDirection.SOUTH), line2D, movementDirection);
                }
            }

            if (movementDirection[3]) { // west
                final ImmutableLocation2D intersection = intersection(
                        location.getX(), location.getY() + 1, location.getX(), location.getY(),
                        line2D.getX1(), line2D.getY1(), line2D.getX2(), line2D.getY2());
                if (intersection != null) {
                    // maxTransition ? return maxTransition : maxTransition at adjacent tile?
                    ret = (location.hasBorder(TileDirection.WEST)) ? intersection
                            : collision(location.getAdjacent(TileDirection.WEST), line2D, movementDirection);
                }
            }
        }

        return ret;
    }
    

    /**
     * Execute the {@code plan}. If the plan will result in a maxTransition, than subject of the {@code plan} will just get rotated, but not translated
     * @param plan the planed movement
     */
    public void executeMovement(MovementPlan<T> plan) {
        checkNotNull(plan);
        checkArgument(projectables.contains(plan.projectable));

        if (!plan.willCollide()) {
            assert(covers(plan.projection));
            plan.projectable.setProjection(plan.projection);
        }

        twoDimTreeOutdated = true;
    }

    public void moveObject(T object2d, Motion2D motion) {
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
        return getTiles().iterator();
    }

    public Iterable<T> getOccupants() {
        return projectables;
    }

    public Iterable<T> getOccupants(final TileLocation tileLocation) {
        return getOccupants(Collections.singleton(tileLocation));
    }

    public Iterable<T> getOccupants(Iterable<? extends TileLocation> tileLocations) {
        return Iterables.concat(Iterables.transform(tileLocations, new Function<TileLocation, Iterable<T>>() {
            @Override
            public Iterable<T> apply(@Nullable final TileLocation tileLocation) {
                return Iterables.filter(projectables, new Predicate<T>() {
                    @Override
                    public boolean apply(T coordinates2D) {
                        assert tileLocation != null;
                        return tileLocation.covers(coordinates2D.getProjection());
                    }
                });
            }
        }));
    }

    public void addObject(T projectable, Object2D projection) {
        checkArgument(this.covers(checkNotNull(projection)));
        checkNotNull(projectable);
        synchronized (this) {
            projectables.add(projectable);
            projectable.setProjection(projection);
            twoDimTreeOutdated = true;
        }
    }

    public boolean removeObject(T motion2D) {
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
    @SuppressWarnings("UnusedDeclaration")
    public static <T extends Simulatable2D> TiledSpace<T> createEmptyCopy(TiledSpace<?> space) {
        return new TiledSpace<T>(space.getWidth(), space.getHeight(), space.getBorderedTiles());
    }

    public int countOccupants() {
        return projectables.size();
    }

    public Iterable<TileLocation> getTiles() {
        return Iterables.concat(Iterables.transform(Arrays.asList(tileMatrix), new Function<TileLocation[], Iterable<TileLocation>>() {
            @Override
            public Iterable<TileLocation> apply(@Nullable TileLocation[] tileLocations) {
                return Arrays.asList(tileLocations);
            }
        }));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TiledSpace)) return false;

        TiledSpace space = (TiledSpace) o;

        if (height != space.height) return false;
        if (width != space.width) return false;
        if (!projectables.equals(space.projectables)) return false;
        if (!Arrays.equals(getBorderedTiles(), space.getBorderedTiles())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = height;
        result = 31 * result + width;
        result = 31 * result + projectables.hashCode();
        result = 31 * result + Arrays.hashCode(getBorderedTiles());
        return result;
    }

    public static class MovementPlan<T extends Projectable<Object2D>> {
        private final T projectable;
        private final Object2D projection;
        private final Location2D collisionPoint;

        private MovementPlan(T projectable, Object2D projection, @Nullable Location2D collisionPoint) {
            this.collisionPoint = collisionPoint;
            assert projectable != null;
            assert projection != null;
            this.projectable = projectable;
            this.projection = projection;
        }

        public boolean willCollide() {
            return collisionPoint != null;
        }
    }

    public static class TiledSpaceBuilder<T extends Projectable<Object2D>> implements Builder<TiledSpace<T>> {

        private final int width;
        private final int height;
        private final List<TiledSpaceBuilder.TileBorderDefinition> borderDefinitions = Lists.newArrayList();

        public TiledSpaceBuilder(int width, int height) {

            this.width = width;
            this.height = height;
        }

        public TiledSpaceBuilder<T> addBorder(int x, int y, TileDirection direction) {
            checkArgument(x >= 0 && x < width && y >= 0 && y < height);
            checkNotNull(direction);
            borderDefinitions.add(new TileBorderDefinition(x, y, direction));
            return this;
        }

        @Override
        public TiledSpace<T> build() throws IllegalStateException {
            return new TiledSpace<T>(this);
        }

        public class TileBorderDefinition {
            private final int x;
            private final int y;
            private final TileDirection direction;

            public TileBorderDefinition(int x, int y, TileDirection direction) {
                this.x = x;
                this.y = y;
                this.direction = direction;
            }
        }
    }
}
