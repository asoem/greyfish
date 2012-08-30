package org.asoem.greyfish.core.space;

import org.asoem.greyfish.utils.space.MovingProjectable2D;

/**
 * User: christoph
 * Date: 30.08.12
 * Time: 09:41
 */
public interface TiledSpace<O extends MovingProjectable2D, T extends Tile> extends Space2D<O>, Tiled<T> {
    Iterable<O> getObjects(Iterable<? extends Tile> tiles);
}
