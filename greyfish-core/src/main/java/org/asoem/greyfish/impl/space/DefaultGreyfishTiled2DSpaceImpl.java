package org.asoem.greyfish.impl.space;

import org.asoem.greyfish.core.space.*;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.utils.space.Point2D;
import org.asoem.greyfish.utils.space.Tile;
import org.asoem.greyfish.utils.space.TileDirection;

/**
 * User: christoph
 * Date: 14.11.12
 * Time: 14:39
 */
public class DefaultGreyfishTiled2DSpaceImpl extends ForwardingSpace2D<Basic2DAgent, Point2D> implements DefaultGreyfishTiled2DSpace {
    private final TiledSpace<Basic2DAgent, Point2D, WalledTile> space;

    public DefaultGreyfishTiled2DSpaceImpl(final TiledSpace<Basic2DAgent, Point2D, WalledTile> space) {
        this.space = space;
    }

    @Override
    protected Space2D<Basic2DAgent, Point2D> delegate() {
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
    public boolean hasTileAt(final int x, final int y) {
        return space.hasTileAt(x, y);
    }

    @Override
    public WalledTile getTileAt(final int x, final int y) {
        return space.getTileAt(x, y);
    }

    @Override
    public Iterable<WalledTile> getTiles() {
        return space.getTiles();
    }

    @Override
    public WalledTile getAdjacentTile(final WalledTile tile, final TileDirection direction) {
        return space.getAdjacentTile(tile, direction);
    }

    @Override
    public Iterable<Basic2DAgent> getObjects(final Iterable<? extends Tile> tiles) {
        return space.getObjects(tiles);
    }

    public static DefaultGreyfishTiled2DSpace ofSize(final int width, final int height) {
        return new DefaultGreyfishTiled2DSpaceImpl(WalledPointSpace.<Basic2DAgent>ofSize(width, height));
    }
}
