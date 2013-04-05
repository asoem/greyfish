package org.asoem.greyfish.core.space;

import com.google.common.base.*;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.math3.util.MathUtils;
import org.asoem.greyfish.utils.base.Builder;
import org.asoem.greyfish.utils.base.SingleElementCache;
import org.asoem.greyfish.utils.collect.Trees;
import org.asoem.greyfish.utils.space.*;

import javax.annotation.Nullable;
import java.util.*;

import static com.google.common.base.Preconditions.*;
import static org.asoem.greyfish.utils.space.Geometry2D.intersection;
import static org.asoem.greyfish.utils.space.Geometry2D.polarToCartesian;

/**
 * @author christoph
 *         This class is used to handle a 2D space implemented as a Matrix of Locations.
 */
public class WalledPointSpace<O> implements TiledSpace<O, Point2D, WalledTile> {

    private final int height;

    private final int width;

    private final WalledTile[][] tileMatrix;

    private final SingleElementCache<TwoDimTree<Point2D, O>> tree = SingleElementCache.memoize(new Supplier<TwoDimTree<Point2D, O>>() {
        @Override
        public TwoDimTree<Point2D, O> get() {
            return treeFactory.create(point2DMap.keySet(), Functions.forMap(point2DMap));
        }
    });

    private final TwoDimTreeFactory<O> treeFactory;

    private final Map<O, Point2D> point2DMap = Maps.newHashMap();

    public WalledPointSpace(WalledPointSpace<O> space) {
        this(checkNotNull(space).colCount(), space.rowCount());
        setWalledTiles(space.getWalledTiles());
    }

    public WalledPointSpace(int width, int height) {
        this(width, height, new WalledTile[0], WalledPointSpace.<O>createDefaultTreeFactory());
    }

    private static <O> TwoDimTreeFactory<O> createDefaultTreeFactory() {
        return new TwoDimTreeFactory<O>() {
            @Override
            public TwoDimTree<Point2D, O> create(Iterable<? extends O> elements, Function<? super O, Point2D> function) {
                return AsoemScalaTwoDimTree.of(elements, function);
            }
        };
    }

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private WalledPointSpace(int width, int height, Map<O, Point2D> projectables, WalledTile[] walledTiles, TwoDimTreeFactory<O> treeFactory) {
        this(width, height, walledTiles, treeFactory);
        this.point2DMap.putAll(projectables);
    }

    private WalledPointSpace(TiledSpaceBuilder<O> builder) {
        this(builder.width, builder.height);
        for (TiledSpaceBuilder.WallDefinition wallDefinition : builder.wallDefinitions) {
            wallDefinition.apply(this);
        }
    }

    public WalledPointSpace(int width, int height, WalledTile[] walledTiles, TwoDimTreeFactory<O> treeFactory) {
        Preconditions.checkArgument(width >= 0);
        Preconditions.checkArgument(height >= 0);
        checkNotNull(treeFactory);

        this.width = width;
        this.height = height;
        this.treeFactory = treeFactory;

        this.tileMatrix = new WalledTile[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                this.tileMatrix[i][j] = new WalledTile(this, i, j);
            }
        }

        setWalledTiles(walledTiles);
    }

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
    public int rowCount() {
        return height;
    }

    @Override
    public int colCount() {
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
    public void moveObject(O object, Motion2D motion) {
        checkNotNull(object);

        //final Motion2D motion = checkNotNull(object.getMotion(), "Required motion of {} is null", object);
        final Point2D currentProjection = checkNotNull(getProjection(object), "Required projection of {} is null", object);

        final double translation = motion.getTranslation();
        final double rotation = motion.getRotation();

        if (translation == 0 && rotation == 0)
            return;
        if (translation < 0)
            throw new IllegalStateException("Translations < 0 are not supported: " + translation);

        final double newOrientation = (rotation == 0)
                ? 0
                : ((rotation) % MathUtils.TWO_PI + MathUtils.TWO_PI) % MathUtils.TWO_PI;

        final Point2D anchorPoint = currentProjection.getCentroid();
        if (translation != 0) {
            final Point2D preferredPoint = ImmutablePoint2D.sum(anchorPoint, polarToCartesian(newOrientation, translation));
            final Point2D maxPoint = maxTransition(anchorPoint, preferredPoint);
            //final MotionObject2D projection = MotionObject2DImpl.of(maxPoint.getX(), maxPoint.getY(), !preferredPoint.equals(maxPoint));
            //object.setProjection(projection);
        }
        else {
            //object.setProjection(MotionObject2DImpl.of(anchorPoint.getX(), anchorPoint.getY(), newOrientation, false));
        }

        tree.invalidate();
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
    public Iterable<O> findObjects(double x, double y, double radius) {
        return tree.get().findObjects(x, y, radius);
    }

    @Override
    public Iterable<O> getVisibleNeighbours(final O object, double range) {
        final Point2D projection = getProjection(object);
        if (projection != null) {
            final Point2D anchorPoint = projection.getCentroid();
            return Iterables.filter(findObjects(anchorPoint.getX(), anchorPoint.getY(), range), new Predicate<O>() {
                @Override
                public boolean apply(O t) {
                    if (t.equals(object))
                        return false;

                    final Point2D neighborProjection = getProjection(t);
                    assert neighborProjection != null;
                    final Point2D neighborProjectionAnchorPoint = neighborProjection.getCentroid();
                    return collision(anchorPoint.getX(), anchorPoint.getY(),
                            neighborProjectionAnchorPoint.getX(), neighborProjectionAnchorPoint.getY()) == null;
                }
            });
        }
        else
            throw new IllegalArgumentException("Projectable has no projection");
    }

    @Override
    public boolean insertObject(O object, Point2D projection) {
        checkNotNull(object, "projectable is null");
        checkNotNull(projection, "projection is null");

        synchronized (this) {
            final Point2D previous = point2DMap.put(object, projection);
            checkState(previous == null, "no duplicate objects allowed: " + object);
            tree.invalidate();
            return true;
        }
    }

    @Override
    public boolean isEmpty() {
        return point2DMap.isEmpty();
    }

    @Override
    public double width() {
        return width;
    }

    @Override
    public double height() {
        return height;
    }

    @Override
    @Nullable
    public Point2D getProjection(final O object) {
        final KDNode<Point2D, O> node = Iterators.find(Trees.postOrderView(tree.get().root()), new Predicate<KDNode<Point2D, O>>() {
            @Override
            public boolean apply(KDNode<Point2D, O> input) {
                return object.equals(input.value());
            }
        }, null);
        return node == null ? null : node.point();
    }

    @Override
    public Map<O, Point2D> asMap() {
        return point2DMap;
    }

    @Override
    public double distance(O agent, double degrees) {
        checkNotNull(agent);
        checkArgument(degrees >= 0 && degrees < MathUtils.TWO_PI, "Degrees must be in [0, TWO_PI), was %s", degrees);

        Point2D borderIntersection;

        final Point2D origin = getProjection(agent);
        if (origin == null)
            throw new IllegalArgumentException("Has no projection: " + agent);

        final ImmutablePoint2D destination = ImmutablePoint2D.sum(origin, Geometry2D.polarToCartesian(degrees, Double.MAX_VALUE));

        if (degrees < 90) {
            borderIntersection = Geometry2D.intersection(origin.getX(), origin.getY(), destination.getX(), destination.getY(),
                    0, 0, width(), 0);
            if (borderIntersection == null)
                borderIntersection = Geometry2D.intersection(origin.getX(), origin.getY(), destination.getX(), destination.getY(),
                        width(), 0, width(), height());
        }
        else if (degrees < 180) {
            borderIntersection = Geometry2D.intersection(origin.getX(), origin.getY(), destination.getX(), destination.getY(),
                    width(), 0, width(), height());
            if (borderIntersection == null)
                borderIntersection = Geometry2D.intersection(origin.getX(), origin.getY(), destination.getX(), destination.getY(),
                        0, height(), width(), height());
        }
        else if (degrees < 270) {
            borderIntersection = Geometry2D.intersection(origin.getX(), origin.getY(), destination.getX(), destination.getY(),
                    0, height(), width(), height());
            if (borderIntersection == null)
                borderIntersection = Geometry2D.intersection(origin.getX(), origin.getY(), destination.getX(), destination.getY(),
                        0, 0, 0, height());
        }
        else {
            borderIntersection = Geometry2D.intersection(origin.getX(), origin.getY(), destination.getX(), destination.getY(),
                    0, 0, 0, height());
            if (borderIntersection == null)
                borderIntersection = Geometry2D.intersection(origin.getX(), origin.getY(), destination.getX(), destination.getY(),
                        0, 0, width(), 0);
        }

        assert borderIntersection != null; // There must always be an intersection with one border

        return Geometry2D.distance(origin, maxTransition(origin, borderIntersection));
    }

    @Override
    public Collection<O> getObjects() {
        return Collections.unmodifiableSet(point2DMap.keySet());
    }

    @Override
    public Iterable<O> getObjects(Iterable<? extends Tile> tiles) {
        return Iterables.concat(Iterables.transform(tiles, new Function<Tile, Iterable<O>>() {
            @Override
            public Iterable<O> apply(Tile tile) {
                final Tile checkedTile = checkNotNull(tile);
                return Iterables.filter(findObjects(checkedTile.getX() + 0.5, checkedTile.getY() + 0.5, 0.70710678118 /* sqrt(0.5^2) */), new Predicate<O>() {
                    @Override
                    public boolean apply(O t) {
                        assert t != null;
                        final Point2D projection = getProjection(t);
                        assert projection != null;
                        final Point2D anchorPoint = projection.getCentroid();
                        return Geometry2D.rectangleContains(checkedTile.getX(), checkedTile.getY(), 1, 1, anchorPoint.getX(), anchorPoint.getY());
                    }
                });
            }
        }));
    }

    @Override
    public boolean removeObject(O agent) {
        checkNotNull(agent);

        synchronized (this) {
            if (point2DMap.remove(agent) != null) {
                tree.invalidate();
                return true;
            } else
                return false;
        }
    }

    @Override
    public boolean removeIf(Predicate<O> predicate) {
        synchronized (this) {
            if (Iterables.removeIf(point2DMap.keySet(), predicate)) {
                tree.invalidate();
                return true;
            }
            return false;
        }
    }

    @Override
    public int countObjects() {
        return point2DMap.size();
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
    public WalledTile getAdjacentTile(WalledTile tile, TileDirection direction) {
        return getTileAt(tile.getX() + direction.getXTranslation(), tile.getY() + direction.getYTranslation());
    }

    public static <O> WalledPointSpace<O> copyOf(WalledPointSpace<O> space) {
        return new WalledPointSpace<O>(space);
    }

    public static <O> WalledPointSpace<O> ofSize(int width, int height) {
        return new WalledPointSpace<O>(width, height);
    }

    public static <O> TiledSpaceBuilder<O> builder(int width, int height) {
        return new TiledSpaceBuilder<O>(width, height);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class TiledSpaceBuilder<O> implements Builder<WalledPointSpace<O>> {

        private final int width;
        private final int height;
        private final List<WallDefinition> wallDefinitions = Lists.newArrayList();
        private TwoDimTreeFactory<O> treeFactory;

        public TiledSpaceBuilder(int width, int height) {

            this.width = width;
            this.height = height;
        }

        public TiledSpaceBuilder<O> addWall(final int x, final int y, final TileDirection direction) {
            checkArgument(x >= 0 && x < width && y >= 0 && y < height);
            checkNotNull(direction);
            wallDefinitions.add(new WallDefinition() {
                @Override
                public void apply(WalledPointSpace<?> space) {
                    space.setTileWall(x, y, direction, true);
                }
            });
            return this;
        }

        public TiledSpaceBuilder<O> addWallsVertical(final int x, final int y1, final int y2, final TileDirection direction) {
            checkArgument(x >= 0 && x < width);
            checkArgument(y1 >= 0 && y1 < height);
            checkArgument(y2 >= 0 && y2 < height);
            checkNotNull(direction);

            wallDefinitions.add(new WallDefinition() {
                @Override
                public void apply(WalledPointSpace<?> space) {
                    for (int i = y1; i <= y2; i++) {
                        space.setTileWall(x, i, direction, true);
                    }
                }
            });
            return this;
        }

        public TiledSpaceBuilder<O> addWallsHorizontal(final int x1, final int x2, final int y, final TileDirection direction) {
            checkArgument(x1 >= 0 && x1 < width);
            checkArgument(x2 >= 0 && x2 < width);
            checkArgument(y >= 0 && y < height);
            checkNotNull(direction);

            wallDefinitions.add(new WallDefinition() {
                @Override
                public void apply(WalledPointSpace<?> space) {
                    for (int i = x1; i <= x2; i++) {
                        space.setTileWall(i, y, direction, true);
                    }
                }
            });
            return this;
        }

        public TiledSpaceBuilder<O> treeFactory(TwoDimTreeFactory<O> treeFactory) {
            this.treeFactory = checkNotNull(treeFactory);
            return this;
        }

        @Override
        public WalledPointSpace<O> build() throws IllegalStateException {
            if (treeFactory == null)
                treeFactory = createDefaultTreeFactory();
            return new WalledPointSpace<O>(this);
        }

        private static interface WallDefinition {
            void apply(WalledPointSpace<?> space);
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
