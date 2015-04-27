/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.core.space;

import org.asoem.greyfish.utils.space.Object2D;
import org.asoem.greyfish.utils.space.Tile;
import org.asoem.greyfish.utils.space.TileDirection;


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
