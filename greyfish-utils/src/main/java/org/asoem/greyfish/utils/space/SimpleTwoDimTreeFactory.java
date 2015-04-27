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

package org.asoem.greyfish.utils.space;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class SimpleTwoDimTreeFactory<T> implements TwoDimTreeFactory<T> {

    private SimpleTwoDimTreeFactory() {
    }

    @Override
    public TwoDimTree<T> create(final Iterable<? extends T> elements, final Function<? super T, Point2D> function) {
        checkNotNull(elements);
        checkNotNull(function);
        return SimpleTwoDimTree.create(createNode(ImmutableList.copyOf(elements), function));
    }

    @Nullable
    private TwoDimTree.Node<T> createNode(final List<T> nodeList, final Function<? super T, Point2D> point2DFunction) {
        switch (nodeList.size()) {
            case 0:
                return null;
            default:
                final T input = nodeList.get(0);
                final Point2D point2D = checkNotNull(point2DFunction.apply(input));
                final TwoDimTree.Node<T> leftChild = createNode(nodeList.subList(1, nodeList.size()), point2DFunction);
                final double x = point2D.getX();
                final double y = point2D.getY();

                return new SimpleTwoDimTree.Node<>(x, y, input,
                        Optional.fromNullable(leftChild), Optional.<TwoDimTree.Node<T>>absent());
        }

    }

    public static <T> TwoDimTreeFactory<T> newInstance() {
        return new SimpleTwoDimTreeFactory<>();
    }

}
