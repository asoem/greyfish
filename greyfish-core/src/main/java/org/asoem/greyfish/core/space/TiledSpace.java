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
import org.asoem.greyfish.utils.space.Tiled;


public interface TiledSpace<O, P extends Object2D, T extends Tile> extends Space2D<O, P>, Tiled<T> {
    Iterable<O> getObjects(Iterable<? extends Tile> tiles);
}
