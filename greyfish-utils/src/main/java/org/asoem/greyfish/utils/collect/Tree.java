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

package org.asoem.greyfish.utils.collect;

import com.google.common.base.Optional;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;

/**
 * A generic tree data structure.
 */
public interface Tree<N> extends Collection<N> {
    /**
     * Get the root node of this tree.
     *
     * @return the root node.
     * @deprecated Use {@link #rootNode()}
     */
    @Nullable
    N root();

    /**
     * Get the optional root node of this tree.
     *
     * @return the root node as an {@code Optional}
     */
    Optional<N> rootNode();

    /**
     * Returns an iterator over the tree elements. The traversal oder is implementation dependent. If you need a
     * specific order use a {@link com.google.common.collect.TreeTraverser TreeTraverser} instead.
     *
     * @return an iterator over the tree elements
     */
    @Override
    Iterator<N> iterator();
}
