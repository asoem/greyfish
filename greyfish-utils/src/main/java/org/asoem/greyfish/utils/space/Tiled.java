package org.asoem.greyfish.utils.space;


public interface Tiled<T extends Tile> {
    int rowCount();

    int colCount();

    boolean hasTileAt(int x, int y);

    T getTileAt(int x, int y);

    Iterable<T> getTiles();

    T getAdjacentTile(T tile, TileDirection direction);
}
