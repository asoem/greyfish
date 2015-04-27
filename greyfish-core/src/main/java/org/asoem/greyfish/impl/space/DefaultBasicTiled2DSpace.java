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

package org.asoem.greyfish.impl.space;

import org.asoem.greyfish.core.space.*;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.utils.space.Point2D;
import org.asoem.greyfish.utils.space.Tile;
import org.asoem.greyfish.utils.space.TileDirection;
import org.asoem.greyfish.utils.space.TwoDimTreeFactory;


public class DefaultBasicTiled2DSpace extends ForwardingSpace2D<Basic2DAgent, Point2D> implements BasicTiled2DSpace {
    private final TiledSpace<Basic2DAgent, Point2D, WalledTile> space;

    public DefaultBasicTiled2DSpace(final TiledSpace<Basic2DAgent, Point2D, WalledTile> space) {
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

    public static BasicTiled2DSpace ofSize(final int width, final int height, final TwoDimTreeFactory<Basic2DAgent> twoDimTreeFactory) {
        return new DefaultBasicTiled2DSpace(WalledPointSpace.<Basic2DAgent>ofSize(width, height, twoDimTreeFactory));
    }
}
