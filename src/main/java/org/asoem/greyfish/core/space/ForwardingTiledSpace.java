package org.asoem.greyfish.core.space;

import org.asoem.greyfish.utils.space.Object2D;
import org.asoem.greyfish.utils.space.Tile;
import org.asoem.greyfish.utils.space.TileDirection;

/**
 * User: christoph
 * Date: 30.08.12
 * Time: 09:39
 */
public abstract class ForwardingTiledSpace<O, P extends Object2D, T extends Tile> extends ForwardingSpace2D<O, P> implements TiledSpace<O, P, T> {

    @Override
    protected abstract TiledSpace<O, P, T> delegate();

    @Override
    public int rowCount() {
        return delegate().rowCount();
    }

    @Override
    public int colCount() {
        return delegate().colCount();
    }

    @Override
    public boolean hasTileAt(final int x, final int y) {
        return delegate().hasTileAt(x, y);
    }

    @Override
    public T getTileAt(final int x, final int y) {
        return delegate().getTileAt(x, y);
    }

    @Override
    public Iterable<T> getTiles() {
        return delegate().getTiles();
    }

    @Override
    public T getAdjacentTile(final T tile, final TileDirection direction) {
        return delegate().getAdjacentTile(tile, direction);
    }

    @Override
    public Iterable<O> getObjects(final Iterable<? extends Tile> tiles) {
        return delegate().getObjects(tiles);
    }

}
