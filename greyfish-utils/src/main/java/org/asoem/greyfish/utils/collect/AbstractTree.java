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

import com.google.common.collect.Iterators;
import com.google.common.collect.TreeTraverser;

import javax.annotation.Nullable;
import java.util.AbstractCollection;
import java.util.Iterator;

public abstract class AbstractTree<T> extends AbstractCollection<T> implements Tree<T> {

    @Override
    public Iterator<T> iterator() {
        if (rootNode().isPresent()) {
            return getTreeTraverser().postOrderTraversal(rootNode().get()).iterator();
        } else {
            return Iterators.emptyIterator();
        }
    }

    @Override
    public int size() {
        return Iterators.size(iterator());
    }

    @Nullable
    @Override
    public T root() {
        return rootNode().orNull();
    }

    protected abstract TreeTraverser<T> getTreeTraverser();
}
