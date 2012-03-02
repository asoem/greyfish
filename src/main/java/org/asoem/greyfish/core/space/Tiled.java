package org.asoem.greyfish.core.space;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 29.02.12
 * Time: 18:52
 */
public interface Tiled<T extends Tile> {
    int getHeight();

    int getWidth();

    boolean hasTileAt(int x, int y);

    T getTileAt(int x, int y);

    Iterable<T> getTiles();

    @Nullable
    T getAdjacentTile(BorderedTile borderedTile, TileDirection direction);
}
