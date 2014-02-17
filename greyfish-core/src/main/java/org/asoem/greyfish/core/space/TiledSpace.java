package org.asoem.greyfish.core.space;

import org.asoem.greyfish.utils.space.Object2D;
import org.asoem.greyfish.utils.space.Tile;
import org.asoem.greyfish.utils.space.Tiled;


public interface TiledSpace<O, P extends Object2D, T extends Tile> extends Space2D<O, P>, Tiled<T> {
    Iterable<O> getObjects(Iterable<? extends Tile> tiles);
}
