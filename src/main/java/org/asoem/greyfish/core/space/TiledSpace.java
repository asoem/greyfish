package org.asoem.greyfish.core.space;

import com.google.common.base.*;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import javolution.lang.MathLib;
import javolution.util.FastList;
import org.asoem.greyfish.utils.base.*;
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

/**
 * @author christoph
 *         This class is used to handle a 2D space implemented as a Matrix of Locations.
 */
public class TiledSpace<T extends MovingProjectable2D> implements Space2D<T>, Tiled<WalledTile> {

    @Attribute(name = "height")
    private final int height;

    @Attribute(name = "width")
    private final int width;

    @ElementList(name = "projectables")
    private final List<T> projectables = FastList.newInstance();

    private final WalledTile[][] tileMatrix;

    private final OutdateableUpdateRequest<Object> updateRequest = UpdateRequests.atomicRequest(true);

    private final Supplier<TwoDimTree<T>> lazyTree = MoreSuppliers.memoize(
            new Supplier<TwoDimTree<T>>() {
                @Override
                public TwoDimTree<T> get() {
                    return AsoemScalaTwoDimTree.create(projectables, new Function<T, Product2<Double, Double>>() {
                        @Override
                        public Point2D apply(T t) {
                            assert t != null;
                            final MotionObject2D projection = t.getProjection();
                            assert projection != null;
                            return projection.getAnchorPoint();
                        }
                    });
                }
            },
            updateRequest);

    public TiledSpace(TiledSpace pSpace) {
        this(checkNotNull(pSpace).getWidth(), pSpace.getHeight());
        setWalledTiles(pSpace.getWalledTiles());
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

        setWalledTiles(walledTiles);
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
     *
     * @param space    The template space
     * @param function A function to the transform the objects in {@code space} into this space
     */
    public TiledSpace(TiledSpace<T> space, Function<T, T> function) {
        this(space.getWidth(), space.getHeight(), space.getWalledTiles());
        Iterables.addAll(projectables, Iterables.transform(space.getObjects(), function));
    }

    public TiledSpace(TiledSpaceBuilder<T> builder) {
        this(builder.width, builder.height);
        for (TiledSpaceBuilder.WallDefinition wallDefinition : builder.wallDefinitions) {
            wallDefinition.apply(this);
        }
    }

    @ElementArray(name = "walledTiles", entry = "tile", required = false)
    private WalledTile[] getWalledTiles() {
        return Iterables.toArray(Iterables.filter(getTiles(), new Predicate<WalledTile>() {
            @Override
            public boolean apply(WalledTile tileLocation) {
                return checkNotNull(tileLocation).getWallFlags() != 0;
            }
        }), WalledTile.class);
    }

    private void setWalledTiles(WalledTile[] tiles) {
        if (tiles != null) {
            for (WalledTile location : tiles)
                getTileAt(location.getX(), location.getY()).setWallFlags(location.getWallFlags());
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
    public boolean contains(double x, double y) {
        return x >= 0 && x < width &&
                y >= 0 && y < height;
    }

    @Override
    public boolean hasTileAt(int x, int y) {
        return contains(x, y);
    }

    @Override
    public WalledTile getTileAt(final int x, final int y) {
        Preconditions.checkArgument(contains(x, y));
        return tileMatrix[x][y];
    }

    @Override
    public String toString() {
        return "Tiled Space: dim=" + width + "x" + height + "; oc=" + Iterables.size(getObjects());
    }

    public WalledTile getTileAt(Point2D point2D) throws IndexOutOfBoundsException, IllegalArgumentException {
        checkArgument(contains(point2D.getX(), point2D.getY()),
                "There is no tile for location [%s, %s]", point2D.getX(), point2D.getY());
        return getTileAt(point2D.getX(), point2D.getY());
    }

    private WalledTile getTileAt(double x, double y) {
        assert x >= 0 && y >= 0; // than we can simply cast the values to int, rather than using Math.floor
        return getTileAt((int) x, (int) y);
    }


    @Override
    public void moveObject(T object) {
        checkNotNull(object);

        final Motion2D motion = checkNotNull(object.getMotion(), "Required motion of {} is null", object);
        final Object2D currentProjection = checkNotNull(object.getProjection(), "Required projection of {} is null", object);

        final double translation = motion.getTranslation();
        final double rotation = motion.getRotation();

        if (translation == 0 && rotation == 0)
            return;
        if (translation < 0)
            throw new IllegalStateException("Translations < 0 are not supported: " + translation);

        final double newOrientation = (rotation == 0)
                ? currentProjection.getOrientationAngle()
                : ((currentProjection.getOrientationAngle() + rotation) % TWO_PI + TWO_PI) % TWO_PI;

        final Point2D anchorPoint = currentProjection.getAnchorPoint();
        if (translation != 0) {
            final Point2D preferredPoint = ImmutablePoint2D.sum(anchorPoint, polarToCartesian(newOrientation, translation));
            final Point2D maxPoint = maxTransition(anchorPoint, preferredPoint);
            final MotionObject2D projection = MotionObject2DImpl.of(maxPoint.getX(), maxPoint.getY(), newOrientation, !preferredPoint.equals(maxPoint));
            object.setProjection(projection);
        }
        else {
            object.setProjection(MotionObject2DImpl.of(anchorPoint.getX(), anchorPoint.getY(), newOrientation, false));
        }

        updateRequest.outdate();
    }

    /**
     * Get the location of the transition from {@code origin} to {@code destination} respecting collision with walls.
     * So, if there is no wall between {@code origin} and {@code destination} that this method returns {@code destination}.
     * Otherwise it returns the first {@code Point2D} at which the line from {@code origin} to {@code destination}
     * intersects with a wall of any crossing tile.
     *
     * @param origin      The origin of the transition
     * @param destination The destination of the transition
     * @return The point of the first collision, or {@code destination} if none occurs
     */
    public Point2D maxTransition(Point2D origin, Point2D destination) {
        final Point2D collision = collision(origin.getX(), origin.getY(), destination.getX(), destination.getY());
        assert collision == null || contains(collision.getX(), collision.getY()) :
                "Calculated maxTransition from " + origin + " to " + destination + " is " + collision + ", which not contained by this space " + this;
        return collision != null ? collision : destination;
    }

    @Nullable
    private Point2D collision(double x, double y, double x1, double y1) {
        return collision(getTileAt(x, y), x, y, x1, y1);
    }

    /**
     * Checks if the line {@code xo, yo, xd, yd} crosses an edge of the {@code tile} or any adjacent tile in the direction of movement which has a wall present.
     * If such a crossing is found, than the point closest to this crossing is returned, {@code null}, otherwise.
     *
     * @param tile the tile to check for a collision
     * @param xo   Movement line x origin
     * @param yo   Movement line y origin
     * @param xd   Movement line x destination
     * @param yd   Movement line x destination
     * @return the location on the line closest to the point of a collision with a wall or {@code null} if none could be found
     */
    @Nullable
    private Point2D collision(WalledTile tile, double xo, double yo, double xd, double yd) {
        assert tile != null;

        if (tile.covers(xd, yd))
            return null;

        TileDirection follow1 = null;
        TileDirection follow2 = null;

        if (yd < yo) { // north
            final ImmutablePoint2D intersection = intersection(
                    tile.getX(), tile.getY(),
                    Math.nextAfter(tile.getX() + 1.0, -Double.MIN_VALUE), tile.getY(),
                    xo, yo, xd, yd);

            if (intersection != null) {
                if (tile.hasWall(TileDirection.NORTH))
                    return intersection;
                else
                    follow1 = TileDirection.NORTH;
            }
        }

        if (xd > xo) { // east
            final ImmutablePoint2D intersection = intersection(
                    Math.nextAfter(tile.getX() + 1.0, -Double.MIN_VALUE), tile.getY(),
                    Math.nextAfter(tile.getX() + 1.0, -Double.MIN_VALUE), Math.nextAfter(tile.getY() + 1.0, -Double.MIN_VALUE),
                    xo, yo, xd, yd);

            if (intersection != null) {
                if (tile.hasWall(TileDirection.EAST)) {
                    return intersection;
                } else {
                    if (follow1 == null) follow1 = TileDirection.EAST;
                    else follow2 = TileDirection.EAST;
                }
            }
        }

        if (yd > yo) { // south
            final ImmutablePoint2D intersection = intersection(
                    tile.getX(), Math.nextAfter(tile.getY() + 1.0, -Double.MIN_VALUE),
                    Math.nextAfter(tile.getX() + 1.0, -Double.MIN_VALUE), Math.nextAfter(tile.getY() + 1.0, -Double.MIN_VALUE),
                    xo, yo, xd, yd);

            if (intersection != null) {
                if (tile.hasWall(TileDirection.SOUTH))
                    return intersection;
                else {
                    if (follow1 == null) follow1 = TileDirection.SOUTH;
                    else follow2 = TileDirection.SOUTH;
                }
            }
        }

        if (xd < xo) { // west
            final ImmutablePoint2D intersection = intersection(
                    tile.getX(), Math.nextAfter(tile.getY() + 1.0, -Double.MIN_VALUE),
                    tile.getX(), tile.getY(),
                    xo, yo, xd, yd);

            if (intersection != null) {
                if (tile.hasWall(TileDirection.WEST))
                    return intersection;
                else {
                    if (follow1 == null) follow1 = TileDirection.WEST;
                    else follow2 = TileDirection.WEST;
                }
            }
        }

        if (follow1 != null && hasAdjacentTile(tile, follow1)) {
            final Point2D collision = collision(getAdjacentTile(tile, follow1), xo, yo, xd, yd);
            if (collision != null)
                return collision;
            else if (follow2 != null && hasAdjacentTile(tile, follow2)) {
                final Point2D collision1 = collision(getAdjacentTile(tile, follow2), xo, yo, xd, yd);
                if (collision1 != null)
                    return collision1;
            }
        }

        return null;
    }

    private boolean hasAdjacentTile(WalledTile tile, TileDirection direction) {
        return hasTileAt(tile.getX() + direction.getXTranslation(), tile.getY() + direction.getYTranslation());
    }

    @Override
    public Iterable<T> findObjects(double x, double y, double range) {
        return lazyTree.get().findObjects(x, y, range);
    }

    public Iterable<T> getVisibleNeighbours(final T agent, double range) {
        final MotionObject2D projection = agent.getProjection();
        if (projection != null) {
            final Point2D anchorPoint = projection.getAnchorPoint();
            return Iterables.filter(findObjects(anchorPoint.getX(), anchorPoint.getY(), range), new Predicate<T>() {
                @Override
                public boolean apply(T t) {
                    if (t.equals(agent))
                        return false;

                    final Object2D neighborProjection = t.getProjection();
                    assert neighborProjection != null;
                    final Point2D neighborProjectionAnchorPoint = neighborProjection.getAnchorPoint();
                    return collision(anchorPoint.getX(), anchorPoint.getY(),
                            neighborProjectionAnchorPoint.getX(), neighborProjectionAnchorPoint.getY()) == null;
                }
            });
        }
        else
            throw new IllegalArgumentException("Agent has no projection");
    }

    public Iterable<T> getNeighbours(T agent, double radius) {
        final MotionObject2D projection = agent.getProjection();
        if (projection != null) {
            final Point2D projectionAnchorPoint = projection.getAnchorPoint();
            return Iterables.filter(
                    findObjects(projectionAnchorPoint.getX(), projectionAnchorPoint.getY(), radius),
                    Predicates.not(Predicates.<Projectable<? extends Object2D>>equalTo(agent)));
        }
        else
            throw new IllegalArgumentException("Agent has no projection");
    }

    @Override
    public List<T> getObjects() {
        return projectables;
    }

    public Iterable<T> getObjects(Iterable<? extends Tile> tiles) {
        return Iterables.concat(Iterables.transform(tiles, new Function<Tile, Iterable<T>>() {
            @Override
            public Iterable<T> apply(Tile tile) {
                final Tile checkedTile = checkNotNull(tile);
                return Iterables.filter(findObjects(checkedTile.getX(), checkedTile.getY(), MathLib.SQRT2 / 2), new Predicate<T>() {
                    @Override
                    public boolean apply(T t) {
                        assert t != null;
                        final Object2D projection = t.getProjection();
                        assert projection != null;
                        final Point2D anchorPoint = projection.getAnchorPoint();
                        return Geometry2D.rectangleContains(checkedTile.getX(), checkedTile.getY(), 1, 1, anchorPoint.getX(), anchorPoint.getY());
                    }
                });
            }
        }));
    }

    @Override
    public void insertObject(T projectable, double x, double y, double orientation) {
        checkNotNull(projectable);
        checkArgument(contains(x, y));

        final MotionObject2D projection = MotionObject2DImpl.of(x, y, orientation, false);
        projectable.setProjection(projection);

        synchronized (this) {
            projectables.add(projectable);
            updateRequest.outdate();
        }
    }

    @Override
    public boolean removeObject(T object) {
        checkNotNull(object);

        synchronized (this) {
            if (projectables.remove(object)) {
                updateRequest.outdate();
                return true;
            } else
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
            public Iterable<WalledTile> apply(WalledTile[] tileLocations) {
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
        if (!Arrays.equals(getWalledTiles(), space.getWalledTiles())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = height;
        result = 31 * result + width;
        result = 31 * result + projectables.hashCode();
        result = 31 * result + Arrays.hashCode(getWalledTiles());
        return result;
    }

    public static <T extends MovingProjectable2D> TiledSpace<T> copyOf(TiledSpace<T> space) {
        return new TiledSpace<T>(space);
    }

    public static <T extends MovingProjectable2D> TiledSpace<T> ofSize(int width, int height) {
        return new TiledSpace<T>(width, height);
    }

    public static <T extends MovingProjectable2D> TiledSpaceBuilder<T> builder(int width, int height) {
        return new TiledSpaceBuilder<T>(width, height);
    }

    /**
     * Create a new {@code TiledSpace} which has the same dimensions and the same borders as the given {@code space},
     * but with no objects.
     *
     * @param space The space to copy the information from
     * @return a new space
     */
    @SuppressWarnings("UnusedDeclaration")
    public static <T extends MovingProjectable2D> TiledSpace<T> createEmptyCopy(TiledSpace<?> space) {
        return new TiledSpace<T>(space.width, space.height, space.getWalledTiles());
    }

    public static class TiledSpaceBuilder<T extends MovingProjectable2D> implements Builder<TiledSpace<T>> {

        private final int width;
        private final int height;
        private final List<WallDefinition> wallDefinitions = Lists.newArrayList();

        public TiledSpaceBuilder(int width, int height) {

            this.width = width;
            this.height = height;
        }

        public TiledSpaceBuilder<T> addWall(final int x, final int y, final TileDirection direction) {
            checkArgument(x >= 0 && x < width && y >= 0 && y < height);
            checkNotNull(direction);
            wallDefinitions.add(new WallDefinition() {
                @Override
                public void apply(TiledSpace<?> space) {
                    space.setTileWall(x, y, direction, true);
                }
            });
            return this;
        }

        public TiledSpaceBuilder<T> addWallsVertical(final int x, final int y1, final int y2, final TileDirection direction) {
            checkArgument(x >= 0 && x < width);
            checkArgument(y1 >= 0 && y1 < height);
            checkArgument(y2 >= 0 && y2 < height);
            checkNotNull(direction);

            wallDefinitions.add(new WallDefinition() {
                @Override
                public void apply(TiledSpace<?> space) {
                    for (int i = y1; i <= y2; i++) {
                        space.setTileWall(x, i, direction, true);
                    }
                }
            });
            return this;
        }

        public TiledSpaceBuilder<T> addWallsHorizontal(final int x1, final int x2, final int y, final TileDirection direction) {
            checkArgument(x1 >= 0 && x1 < width);
            checkArgument(x2 >= 0 && x2 < width);
            checkArgument(y >= 0 && y < height);
            checkNotNull(direction);

            wallDefinitions.add(new WallDefinition() {
                @Override
                public void apply(TiledSpace<?> space) {
                    for (int i = x1; i <= x2; i++) {
                        space.setTileWall(i, y, direction, true);
                    }
                }
            });
            return this;
        }

        @Override
        public TiledSpace<T> build() throws IllegalStateException {
            return new TiledSpace<T>(this);
        }

        private static interface WallDefinition {
            void apply(TiledSpace<?> space);
        }
    }

    /**
     * Set (b={@code true}) or unset (b={@code false}) the wall of the tile at x,y in the given direction.
     * Automatically adjusts the wall in the opposite direction at the adjacent tile in the given {@code direction}
     *
     * @param x         x of the tile location
     * @param y         y of the tile location
     * @param direction the side of the tile which will get modified
     * @param b         indicates if the wall will be set or unset
     */
    private void setTileWall(int x, int y, TileDirection direction, boolean b) {
        final WalledTile tile = getTileAt(x, y);
        tile.setWall(direction, b);

        if (hasAdjacentTile(tile, direction)) {
            getAdjacentTile(tile, direction).setWall(direction.opposite(), b);
        }
    }

}
