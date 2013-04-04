package org.asoem.greyfish.core.space;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.utils.space.Point2D;
import org.asoem.greyfish.utils.space.Tile;
import org.asoem.greyfish.utils.space.TileDirection;

/**
 * User: christoph
 * Date: 14.11.12
 * Time: 14:39
 */
public class DefaultGreyfishSpaceImpl extends ForwardingSpace2D<DefaultGreyfishAgent, Point2D> implements DefaultGreyfishSpace {
    private final TiledSpace<DefaultGreyfishAgent, Point2D, WalledTile> space;

    public DefaultGreyfishSpaceImpl(TiledSpace<DefaultGreyfishAgent, Point2D, WalledTile> space) {
        this.space = space;
    }

    @Override
    protected Space2D<DefaultGreyfishAgent, Point2D> delegate() {
        return space;
    }

    @Override
    public int rowCount() {
        return space.rowCount();
    }

    @Override
    public int colCount() {
        return space.colCount();
    }

    @Override
    public boolean hasTileAt(int x, int y) {
        return space.hasTileAt(x, y);
    }

    @Override
    public WalledTile getTileAt(int x, int y) {
        return space.getTileAt(x, y);
    }

    @Override
    public Iterable<WalledTile> getTiles() {
        return space.getTiles();
    }

    @Override
    public WalledTile getAdjacentTile(WalledTile tile, TileDirection direction) {
        return space.getAdjacentTile(tile, direction);
    }

    @Override
    public Iterable<DefaultGreyfishAgent> getObjects(Iterable<? extends Tile> tiles) {
        return space.getObjects(tiles);
    }

    public static DefaultGreyfishSpace ofSize(int width, int height) {
        return new DefaultGreyfishSpaceImpl(WalledPointSpace.<DefaultGreyfishAgent>ofSize(width, height));
    }
}
