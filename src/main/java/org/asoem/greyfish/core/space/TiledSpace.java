package org.asoem.greyfish.core.space;

import org.asoem.greyfish.utils.space.Tile;
import org.asoem.greyfish.utils.space.Tiled;

/**
 * User: christoph
 * Date: 30.08.12
 * Time: 09:41
 */
public interface TiledSpace<O, T extends Tile> extends Space2D<O>, Tiled<T> {
    Iterable<O> getObjects(Iterable<? extends Tile> tiles);
}
