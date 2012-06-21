package org.asoem.greyfish.core.space;

import com.google.common.base.*;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import javolution.lang.MathLib;
import javolution.util.FastList;
import org.asoem.greyfish.core.simulation.Simulatable2D;
import org.asoem.greyfish.utils.base.Builder;
import org.asoem.greyfish.utils.space.*;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementArray;
import org.simpleframework.xml.ElementList;

import javax.annotation.Nullable;
import java.util.Arrays;
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
public class TiledSpace<T extends Projectable<Object2D>> implements Space2D<T>, Tiled<WalledTile> {

    @Attribute(name = "height")
    private final int height;

    @Attribute(name = "width")
    private final int width;

    @ElementList(name = "projectables")
    private final List<T> projectables = FastList.newInstance();

    private final WalledTile[][] tileMatrix;

    private final SelfUpdatingTree<T> tree = new SelfUpdatingTree<T>(AsoemScalaTwoDimTree.<T>newInstance(), new Supplier<Iterable<? extends T>>() {
        @Override
        public Iterable<? extends T> get() {
            return projectables;
        }
    }, new Function<T, Location2D>() {
        @Override
        public Location2D apply(@Nullable T t) {
            assert t != null;
            return t.getProjection();
        }
    });

    public TiledSpace(TiledSpace pSpace) {
        this(checkNotNull(pSpace).getWidth(), pSpace.getHeight());
        setBorderedTiles(pSpace.getBorderedTiles());
    }

    public TiledSpace(int width, int height) {
        this(width, height, new WalledTile[0]);
    }

    public TiledSpace(int width, int height, WalledTile[] walledTiles) {
        Preconditions.checkArgument(width >= 0);
        Preconditions.checkArgument(height >= 0);

        this.width = width;
        this.height = height;

        this.tileMatrix = new WalledTile[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                this.tileMatrix[i][j] = new WalledTile(this, i, j);
            }
        }

        setBorderedTiles(walledTiles);
    }

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private TiledSpace(@Attribute(name = "width") int width,
                       @Attribute(name = "height") int height,
                       @ElementList(name = "projectables") List<T> projectables,
                       @ElementArray(name = "walledTiles", entry = "tile", required = false) WalledTile[] walledTiles) {
        this(width, height, walledTiles);
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
        Iterables.addAll(projectables, Iterables.transform(space.getObjects(), function));
    }

    public TiledSpace(TiledSpaceBuilder<T> builder) {
        this(builder.width, builder.height);
        for (TiledSpaceBuilder.BorderDefinition borderDefinition : builder.borderDefinitions) {
            borderDefinition.apply(this);
        }
    }

    @ElementArray(name = "walledTiles", entry = "tile", required = false)
    private WalledTile[] getBorderedTiles() {
        return Iterables.toArray(Iterables.filter(getTiles(), new Predicate<WalledTile>() {
            @Override
            public boolean apply(WalledTile tileLocation) {
                return checkNotNull(tileLocation).getBorderFlags() != 0;
            }
        }), WalledTile.class);
    }

    private void setBorderedTiles(WalledTile[] tiles) {
        if (tiles != null) {
            for (WalledTile location : tiles)
                getTileAt(location.getX(), location.getY()).setBorderFlags(location.getBorderFlags());
        }
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public boolean contains(Location2D location) {
        checkNotNull(location);
        return Geometry2D.rectangleContains(0, 0, getWidth(), getHeight(), location.getX(), location.getY());
    }

    @Override
    public boolean hasTileAt(int x, int y) {
        return x >= 0 && x < width &&
                y >= 0 && y < height;
    }

    @Override
    public WalledTile getTileAt(final int x, final int y) {
        Preconditions.checkArgument(hasTileAt(x, y));
        return tileMatrix[x][y];
    }

    @Override
    public String toString() {
        return "Tiled Space: dim="+width+"x"+height+"; oc="+Iterables.size(getObjects());
    }

    public WalledTile getTileAt(Location2D location2D) throws IndexOutOfBoundsException, IllegalArgumentException {
        checkArgument(contains(location2D), "There is no tile for location [%s, %s]", location2D.getX(), location2D.getY());
        return getTileAt(location2D.getX(), location2D.getY());
    }

    private WalledTile getTileAt(double x, double y) {
        assert x >= 0 && y >= 0; // than we can simply cast the values to int, rather than using Math.floor
        return getTileAt((int) x, (int) y);
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

        final double newOrientation = ((currentProjection.getOrientationAngle() + motion2D.getRotation()) % TWO_PI + TWO_PI) % TWO_PI;
        final double translation = motion2D.getTranslation();
        final Location2D preferredLocation = sum(currentProjection, polarToCartesian(newOrientation, translation));
        final Location2D maxLocation = maxTransition(currentProjection, preferredLocation);

        return new MovementPlan<T>(agent, preferredLocation, maxLocation, newOrientation);
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
        final Location2D collision = collision(origin.getX(), origin.getY(), destination.getX(), destination.getY());
        assert collision == null || contains(collision) :
                "Calculated maxTransition from " + origin + " to " + destination + " is " + collision + ", which not contained by this space " + this;
        return collision != null ? collision : destination;
    }

    @Nullable
    private Location2D collision(double x, double y, double x1, double y1) {
        return collision(getTileAt(x, y), x, y, x1, y1);
    }

    /**
     * Checks if the line {@code xo, yo, xd, yd} crosses an edge of the {@code tile} or any adjacent tile in the direction of movement which has a wall present.
     * If such a crossing is found, than the point closest to this crossing is returned, {@code null}, otherwise.
     * @param tile the tile to check for a collision
     * @param xo Movement line x origin
     * @param yo Movement line y origin
     * @param xd Movement line x destination
     * @param yd Movement line x destination
     * @return the location on the line closest to the point of a collision with a wall or {@code null} if none could be found
     */
    @Nullable
    private Location2D collision(WalledTile tile, double xo, double yo, double xd, double yd) {
        assert tile != null;

        if (tile.covers(xd, yd))
            return null;

        TileDirection follow1 = null;
        TileDirection follow2 = null;

        if (yd < yo) { // north
            final ImmutableLocation2D intersection = intersection(
                    tile.getX(), tile.getY(),
                    Math.nextAfter(tile.getX() + 1.0, -Double.MIN_VALUE), tile.getY(),
                    xo, yo, xd, yd);

            if (intersection != null) {
                if (tile.hasBorder(TileDirection.NORTH))
                    return intersection;
                else
                    follow1 = TileDirection.NORTH;
            }
        }

        if (xd > xo) { // east
            final ImmutableLocation2D intersection = intersection(
                    Math.nextAfter(tile.getX() + 1.0, -Double.MIN_VALUE), tile.getY(),
                    Math.nextAfter(tile.getX() + 1.0, -Double.MIN_VALUE), Math.nextAfter(tile.getY() + 1.0, -Double.MIN_VALUE),
                    xo, yo, xd, yd);

            if (intersection != null) {
                if (tile.hasBorder(TileDirection.EAST)) {
                    return intersection;
                }
                else { if (follow1 == null) follow1 = TileDirection.EAST; else follow2 = TileDirection.EAST; }
            }
        }

        if (yd > yo) { // south
            final ImmutableLocation2D intersection = intersection(
                    tile.getX(), Math.nextAfter(tile.getY() + 1.0, -Double.MIN_VALUE),
                    Math.nextAfter(tile.getX() + 1.0, -Double.MIN_VALUE), Math.nextAfter(tile.getY() + 1.0, -Double.MIN_VALUE),
                    xo, yo, xd, yd);

            if (intersection != null) {
                if (tile.hasBorder(TileDirection.SOUTH))
                    return intersection;
                else { if (follow1 == null) follow1 = TileDirection.SOUTH; else follow2 = TileDirection.SOUTH; }
            }
        }

        if (xd < xo) { // west
            final ImmutableLocation2D intersection = intersection(
                    tile.getX(), Math.nextAfter(tile.getY() + 1.0, -Double.MIN_VALUE),
                    tile.getX(), tile.getY(),
                    xo, yo, xd, yd);

            if (intersection != null) {
                if (tile.hasBorder(TileDirection.WEST))
                    return intersection;
                else { if (follow1 == null) follow1 = TileDirection.WEST; else follow2 = TileDirection.WEST; }
            }
        }

        if (follow1 != null && hasAdjacentTile(tile, follow1)) {
            final Location2D collision = collision(getAdjacentTile(tile, follow1), xo, yo, xd, yd);
            if (collision != null)
                return collision;
            else if (follow2 != null && hasAdjacentTile(tile, follow2)) {
                final Location2D collision1 = collision(getAdjacentTile(tile, follow2), xo, yo, xd, yd);
                if (collision1 != null)
                    return collision1;
            }
        }

        return null;
    }

    private boolean hasAdjacentTile(WalledTile tile, TileDirection direction) {
        return hasTileAt(tile.getX() + direction.getXTranslation(), tile.getY() + direction.getYTranslation());
    }

    /**
     * Execute the {@code plan}. If the plan will result in a maxTransition, than subject of the {@code plan} will just get rotated, but not translated
     * @param plan the planed movement
     * @return the projection after the movement
     */
    private Object2D executeMovement(MovementPlan<T> plan) {
        checkNotNull(plan);
        //checkArgument(projectables.contains(plan.projectable)); // todo: the call to 'contains' is a CPU hotspot because agents have a complex equals method

        final Location2D maxLocation = plan.getMaxLocation();
        assert contains(maxLocation) : maxLocation;

        final ImmutableObject2D projection = ImmutableObject2D.of(maxLocation.getX(), maxLocation.getY(), plan.getNewOrientation());
        plan.projectable.setProjection(projection);
        tree.setOutdated();

        return projection;
    }

    @Override
    public Object2D moveObject(T object2d, Motion2D motion) {
        return executeMovement(planMovement(object2d, motion));
    }

    @Override
    public Iterable<T> findObjects(Location2D point, double range) {
        return tree.findObjects(point, range);
    }

    public Iterable<T> getVisibleNeighbours(final T agent, double range) {
        return Iterables.filter(findObjects(agent.getProjection(), range), new Predicate<T>() {
            @Override
            public boolean apply(T t) {
                if (t.equals(agent))
                    return false;

                final Object2D neighborProjection = t.getProjection();
                assert neighborProjection != null;
                final Object2D agentProjection = agent.getProjection();
                assert agentProjection != null;
                return collision(agentProjection.getX(), agentProjection.getY(),
                        neighborProjection.getX(), neighborProjection.getY()) == null;
            }
        });
    }

    public Iterable<T> getNeighbours(T agent, double radius) {
        return Iterables.filter(
                findObjects(agent.getProjection(), radius),
                Predicates.not(Predicates.<Projectable<? extends Object2D>>equalTo(agent)));
    }

    @Override
    public Iterable<T> getObjects() {
        return projectables;
    }

    public Iterable<T> getObjects(Iterable<? extends Tile> tileLocations) {
        return Iterables.concat(Iterables.transform(tileLocations, new Function<Tile, Iterable<T>>() {
            @Override
            public Iterable<T> apply(@Nullable Tile tile) {
                final Tile checkedTile = checkNotNull(tile);
                return Iterables.filter(findObjects(ImmutableLocation2D.at(checkedTile.getX() + 0.5, checkedTile.getY() + 0.5), MathLib.SQRT2 / 2), new Predicate<T>() {
                    @Override
                    public boolean apply(@Nullable T t) {
                        assert t != null;
                        final Object2D projection = t.getProjection();
                        assert projection != null;
                        return Geometry2D.rectangleContains(checkedTile.getX(), checkedTile.getY(), 1, 1, projection.getX(), projection.getY());
                    }
                });
            }
        }));
    }

    @Override
    public void addObject(T projectable, Object2D projection) {
        checkArgument(this.contains(checkNotNull(projection)));
        checkNotNull(projectable);
        synchronized (this) {
            projectables.add(projectable);
            projectable.setProjection(projection);
            tree.setOutdated();
        }
    }

    @Override
    public boolean removeObject(T object) {
        checkNotNull(object);
        synchronized (this) {
            if (projectables.remove(object)) {
                tree.setOutdated();
                return true;
            }
            else
                return false;
        }
    }

    @Override
    public int countObjects() {
        return projectables.size();
    }

    @Override
    public Iterable<WalledTile> getTiles() {
        return Iterables.concat(Iterables.transform(Arrays.asList(tileMatrix), new Function<WalledTile[], Iterable<WalledTile>>() {
            @Override
            public Iterable<WalledTile> apply(@Nullable WalledTile[] tileLocations) {
                return Arrays.asList(tileLocations);
            }
        }));
    }

    @Override
    public WalledTile getAdjacentTile(WalledTile walledTile, TileDirection direction) {
        return getTileAt(walledTile.getX() + direction.getXTranslation(), walledTile.getY() + direction.getYTranslation());
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tiled)) return false;

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

    public static Tiled copyOf(TiledSpace space) {
        return new TiledSpace(space);
    }

    public static <T extends Projectable<Object2D>> TiledSpace<T> ofSize(int width, int height) {
        return new TiledSpace<T>(width, height);
    }

    public static <T extends Projectable<Object2D>> TiledSpaceBuilder<T> builder(int width, int height) {
        return new TiledSpaceBuilder<T>(width, height);
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

    public static class MovementPlan<T extends Projectable<Object2D>> {
        private final T projectable;
        private final Location2D preferredLocation;
        private final Location2D maxLocation;
        private final double newOrientation;

        public MovementPlan(T agent, Location2D preferredLocation, Location2D maxLocation, double newOrientation) {
            this.projectable = agent;
            this.preferredLocation = preferredLocation;
            this.maxLocation = maxLocation;
            this.newOrientation = newOrientation;
        }

        public boolean willCollide() {
            return preferredLocation != maxLocation;
        }

        @SuppressWarnings("UnusedDeclaration")
        public Location2D getPreferredLocation() {
            return preferredLocation;
        }

        public Location2D getMaxLocation() {
            return maxLocation;
        }

        public double getNewOrientation() {
            return newOrientation;
        }
    }

    public static class TiledSpaceBuilder<T extends Projectable<Object2D>> implements Builder<TiledSpace<T>> {

        private final int width;
        private final int height;
        private final List<BorderDefinition> borderDefinitions = Lists.newArrayList();

        public TiledSpaceBuilder(int width, int height) {

            this.width = width;
            this.height = height;
        }

        public TiledSpaceBuilder<T> addBorder(final int x, final int y, final TileDirection direction) {
            checkArgument(x >= 0 && x < width && y >= 0 && y < height);
            checkNotNull(direction);
            borderDefinitions.add(new BorderDefinition() {
                @Override
                public void apply(TiledSpace<?> space) {
                    space.getTileAt(x, y).setBorder(direction, true);
                }
            });
            return this;
        }

        public TiledSpaceBuilder<T> addBordersVertical(final int x, final int y1, final int y2, final TileDirection direction) {
            checkArgument(x >= 0 && x < width);
            checkArgument(y1 >= 0 && y1 < height);
            checkArgument(y2 >= 0 && y2 < height);
            checkNotNull(direction);

            borderDefinitions.add(new BorderDefinition() {
                @Override
                public void apply(TiledSpace<?> space) {
                    for (int i = y1; i <= y2; i++) {
                        space.getTileAt(x, i).setBorder(direction, true);
                    }
                }
            });
            return this;
        }

        public TiledSpaceBuilder<T> addBordersHorizontal(final int x1, final int x2, final int y, final TileDirection direction) {
            checkArgument(x1 >= 0 && x1 < width);
            checkArgument(x2 >= 0 && x2 < width);
            checkArgument(y >= 0 && y < height);
            checkNotNull(direction);

            borderDefinitions.add(new BorderDefinition() {
                @Override
                public void apply(TiledSpace<?> space) {
                    for (int i = x1; i <= x2; i++) {
                        space.getTileAt(i, y).setBorder(direction, true);
                    }
                }
            });
            return this;
        }

        @Override
        public TiledSpace<T> build() throws IllegalStateException {
            return new TiledSpace<T>(this);
        }

        private static interface BorderDefinition {
            void apply(TiledSpace<?> space);
        }
    }

}
