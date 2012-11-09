package org.asoem.greyfish.core.space;

import org.asoem.greyfish.utils.space.MovingProjectable2D;
import org.asoem.greyfish.utils.space.Tile;
import org.asoem.greyfish.utils.space.TileDirection;

/**
 * User: christoph
 * Date: 30.08.12
 * Time: 09:39
 */
public abstract class ForwardingTiledSpace<O extends MovingProjectable2D, T extends Tile> extends ForwardingSpace2D<O> implements TiledSpace<O, T> {

    @Override
    protected abstract TiledSpace<O, T> delegate();

    @Override
    public int rowCount() {
        return delegate().rowCount();
    }

    @Override
    public int colCount() {
        return delegate().colCount();
    }

    @Override
    public boolean hasTileAt(int x, int y) {
        return delegate().hasTileAt(x, y);
    }

    @Override
    public T getTileAt(int x, int y) {
        return delegate().getTileAt(x, y);
    }

    @Override
    public Iterable<T> getTiles() {
        return delegate().getTiles();
    }

    @Override
    public T getAdjacentTile(T borderedTile, TileDirection direction) {
        return delegate().getAdjacentTile(borderedTile, direction);
    }

    @Override
    public Iterable<O> getObjects(Iterable<? extends Tile> tiles) {
        return delegate().getObjects(tiles);
    }

}
