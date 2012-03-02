package org.asoem.greyfish.core.space;

import com.google.common.base.*;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import javolution.lang.MathLib;
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
public class TiledSpace<T extends Projectable<Object2D>> implements Space2D<T>, Tiled<BorderedTile> {

    public static final double CLOSEST_TO_ZERO_RIGHT = 0.000000000001;

    public static final double CLOSEST_TO_ONE_LEFT = 0.999999999999;

    @Attribute(name = "height")
    private final int height;

    @Attribute(name = "width")
    private final int width;

    @ElementList(name = "projectables")
    private final List<T> projectables = FastList.newInstance();

    private final BorderedTile[][] tileMatrix;

    private final SelfUpdatingTree twoDimTree = new SelfUpdatingTree();

    public TiledSpace(TiledSpace pSpace) {
        this(checkNotNull(pSpace).getWidth(), pSpace.getHeight());
        setBorderedTiles(pSpace.getBorderedTiles());
    }

    public TiledSpace(int width, int height) {
        this(width, height, new BorderedTile[0]);
    }

    public TiledSpace(int width, int height, BorderedTile[] borderedTiles) {
        Preconditions.checkArgument(width >= 0);
        Preconditions.checkArgument(height >= 0);

        this.width = width;
        this.height = height;

        this.tileMatrix = new BorderedTile[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                this.tileMatrix[i][j] = new BorderedTile(this, i, j);
            }
        }

        setBorderedTiles(borderedTiles);
    }

    @SimpleXMLConstructor
    private TiledSpace(@Attribute(name = "width") int width,
                       @Attribute(name = "height") int height,
                       @ElementList(name = "projectables") List<T> projectables,
                       @ElementArray(name = "borderedTiles", entry = "tile", required = false) BorderedTile[] borderedTiles) {
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
        Iterables.addAll(projectables, Iterables.transform(space.getObjects(), function));
    }

    public TiledSpace(TiledSpaceBuilder<T> builder) {
        this(builder.width, builder.height);
        for (TiledSpaceBuilder.TileBorderDefinition borderDefinition : builder.borderDefinitions) {
            getTileAt(borderDefinition.x, borderDefinition.y).setBorder(borderDefinition.direction, true);
        }
    }

    @ElementArray(name = "borderedTiles", entry = "tile", required = false)
    private BorderedTile[] getBorderedTiles() {
        return Iterables.toArray(Iterables.filter(getTiles(), new Predicate<BorderedTile>() {
            @Override
            public boolean apply(BorderedTile tileLocation) {
                return checkNotNull(tileLocation).getBorderFlags() != 0;
            }
        }), BorderedTile.class);
    }

    private void setBorderedTiles(BorderedTile[] tiles) {
        if (tiles != null) {
            for (BorderedTile location : tiles)
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
    public BorderedTile getTileAt(final int x, final int y) {
        Preconditions.checkArgument(hasTileAt(x, y));
        return tileMatrix[x][y];
    }

    @Override
    public String toString() {
        return "Tiled Space: dim="+width+"x"+height+"; oc="+Iterables.size(getObjects());
    }

    public BorderedTile getTileAt(Location2D location2D) throws IndexOutOfBoundsException, IllegalArgumentException {
        checkArgument(contains(location2D), "There is no tile for location [%s, %s]", location2D.getX(), location2D.getY());
        return getTileAt(location2D.getX(), location2D.getY());
    }

    private BorderedTile getTileAt(double x, double y) {
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
    private Location2D collision(@Nullable BorderedTile tile, Line2D line2D, boolean [] movementDirection) {
        assert movementDirection.length == 4;
        assert line2D != null;

        Location2D ret = null;

        if (tile != null && line2D.intersects(tile.getX(), tile.getY(), CLOSEST_TO_ONE_LEFT, CLOSEST_TO_ONE_LEFT)) {
            if (movementDirection[0]) { // north
                final ImmutableLocation2D intersection = intersection(
                        tile.getX(), tile.getY(), tile.getX() + CLOSEST_TO_ONE_LEFT, tile.getY(),
                        line2D.getX1(), line2D.getY1(), line2D.getX2(), line2D.getY2());
                if (intersection != null) {
                    // maxTransition ? return maxTransition : maxTransition at adjacent tile?
                    ret = (tile.hasBorder(TileDirection.NORTH)) ? intersection
                            : collision(getAdjacentTile(tile, TileDirection.NORTH), line2D, movementDirection);
                }
            }

            if (ret == null && movementDirection[1]) { // east
                final ImmutableLocation2D intersection = intersection(
                        tile.getX() + CLOSEST_TO_ONE_LEFT, tile.getY(), tile.getX() + CLOSEST_TO_ONE_LEFT, tile.getY() + CLOSEST_TO_ONE_LEFT,
                        line2D.getX1(), line2D.getY1(), line2D.getX2(), line2D.getY2());
                if (intersection != null) {
                    // maxTransition ? return maxTransition : maxTransition at adjacent tile?
                    ret = (tile.hasBorder(TileDirection.EAST)) ? intersection
                            : collision(getAdjacentTile(tile, TileDirection.EAST), line2D, movementDirection);
                }
            }

            if (ret == null && movementDirection[2]) { // south
                final ImmutableLocation2D intersection = intersection(
                        tile.getX() + CLOSEST_TO_ONE_LEFT, tile.getY() + CLOSEST_TO_ONE_LEFT, tile.getX(), tile.getY() + CLOSEST_TO_ONE_LEFT,
                        line2D.getX1(), line2D.getY1(), line2D.getX2(), line2D.getY2());
                if (intersection != null) {
                    // maxTransition ? return maxTransition : maxTransition at adjacent tile?
                    ret = (tile.hasBorder(TileDirection.SOUTH)) ? intersection
                            : collision(getAdjacentTile(tile, TileDirection.SOUTH), line2D, movementDirection);
                }
            }

            if (movementDirection[3]) { // west
                final ImmutableLocation2D intersection = intersection(
                        tile.getX(), tile.getY() + CLOSEST_TO_ONE_LEFT, tile.getX(), tile.getY(),
                        line2D.getX1(), line2D.getY1(), line2D.getX2(), line2D.getY2());
                if (intersection != null) {
                    // maxTransition ? return maxTransition : maxTransition at adjacent tile?
                    ret = (tile.hasBorder(TileDirection.WEST)) ? intersection
                            : collision(getAdjacentTile(tile, TileDirection.WEST), line2D, movementDirection);
                }
            }
        }

        return ret;
    }

    /**
     * Execute the {@code plan}. If the plan will result in a maxTransition, than subject of the {@code plan} will just get rotated, but not translated
     * @param plan the planed movement
     * @return the projection after the movement
     */
    private Object2D executeMovement(MovementPlan<T> plan) {
        checkNotNull(plan);
        checkArgument(projectables.contains(plan.projectable));

        assert contains(plan.getMaxLocation()) : plan.getMaxLocation();

        final ImmutableObject2D projection = ImmutableObject2D.of(plan.getMaxLocation(), plan.getNewOrientation());
        plan.projectable.setProjection(projection);
        twoDimTree.setOutdated();

        return projection;
    }

    @Override
    public Object2D moveObject(T object2d, Motion2D motion) {
        return executeMovement(planMovement(object2d, motion));
    }

    @Override
    public Iterable<T> findObjects(Location2D point, double range) {
        return twoDimTree.findObjects(point, range);
    }

    public Iterable<T> getNeighbours(T agent, double radius) {
        return Iterables.filter(findObjects(agent.getProjection(), radius), Predicates.not(Predicates.<Projectable<? extends Object2D>>equalTo(agent)));
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
            twoDimTree.setOutdated();
        }
    }

    @Override
    public boolean removeObject(T object) {
        checkNotNull(object);
        synchronized (this) {
            if (projectables.remove(object)) {
                twoDimTree.setOutdated();
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
    public Iterable<BorderedTile> getTiles() {
        return Iterables.concat(Iterables.transform(Arrays.asList(tileMatrix), new Function<BorderedTile[], Iterable<BorderedTile>>() {
            @Override
            public Iterable<BorderedTile> apply(@Nullable BorderedTile[] tileLocations) {
                return Arrays.asList(tileLocations);
            }
        }));
    }

    @Override
    public BorderedTile getAdjacentTile(BorderedTile borderedTile, TileDirection direction) {
        return getTileAt(borderedTile.getX() + direction.xTranslation, borderedTile.getY() + direction.yTranslation);
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

    private class SelfUpdatingTree implements TwoDimTree<T> {

            private final TwoDimTree<T> delegate = AsoemScalaTwoDimTree.newInstance();
            private boolean outdated = false;

            @Override
            public void rebuild(Iterable<? extends T> elements, Function<? super T, ? extends Location2D> function) {
                delegate.rebuild(elements, function);
            }

            @Override
            public Iterable<T> findObjects(Location2D locatable, double range) {
                if (outdated)
                    rebuild();
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

        private void rebuild() {
            rebuild(projectables, new Function<Projectable<Object2D>, Location2D>() {
                @Override
                public Location2D apply(@Nullable Projectable<Object2D> o) {
                    assert o != null;
                    return o.getProjection();
                }
            });
        }

        @Override
        public Iterator<T> iterator() {
            if(outdated)
                rebuild();
            return delegate.iterator();
        }

        public void setOutdated() {
            outdated = true;
        }
    }
}
