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

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.*;

import javax.annotation.Nullable;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple implementation of a two dimensional kd-tree with no search optimization.
 *
 * @param <T> the value type
 */
public final class SimpleTwoDimTree<T> extends AbstractCollection<TwoDimTree.Node<T>> implements TwoDimTree<T> {

    private final Optional<TwoDimTree.Node<T>> rootNode;
    private final TreeTraverser<TwoDimTree.Node<T>> treeTraverser = new TwoDimTreeTraverser<>();
    private final Supplier<Integer> lazySize = Suppliers.memoize(new Supplier<Integer>() {
        @Override
        public Integer get() {
            return Iterables.size(SimpleTwoDimTree.this);
        }
    });

    private SimpleTwoDimTree(@Nullable final TwoDimTree.Node<T> rootNode) {
        this.rootNode = Optional.fromNullable(rootNode);
    }

    public static <T> SimpleTwoDimTree<T> create(final TwoDimTree.Node<T> rootNode) {
        return new SimpleTwoDimTree<>(rootNode);
    }

    @Override
    public int dimensions() {
        return 2;
    }

    @Override
    public int size() {
        return lazySize.get();
    }

    @Override
    public Iterable<DistantObject<TwoDimTree.Node<T>>> rangeSearch(final double[] center, final double range) {
        checkArgument(center.length == dimensions(), "Dimension mismatch");
        return ImmutableList.copyOf(findNodes(center[0], center[1], range));
    }

    @Nullable
    @Override
    public TwoDimTree.Node<T> root() {
        return rootNode().orNull();
    }

    @Override
    public Optional<TwoDimTree.Node<T>> rootNode() {
        return rootNode;
    }

    @Override
    public Iterable<DistantObject<TwoDimTree.Node<T>>> findNodes(final double x, final double y, final double range) {
        final FluentIterable<TwoDimTree.Node<T>> nodes = treeTraverser.preOrderTraversal(rootNode().get());
        List<DistantObject<TwoDimTree.Node<T>>> distantObjects = Lists.newArrayList();
        for (final TwoDimTree.Node<T> node : nodes) {
            final double distance = ImmutablePoint2D.at(x, y).distance(
                    ImmutablePoint2D.at(node.xCoordinate(), node.yCoordinate()));
            if (distance <= range) {
                distantObjects.add(new DistantObject<TwoDimTree.Node<T>>() {
                    @Override
                    public TwoDimTree.Node<T> object() {
                        return node;
                    }

                    @Override
                    public double distance() {
                        return distance;
                    }
                });
            }
        }
        return distantObjects;
    }

    @Override
    public Iterator<TwoDimTree.Node<T>> iterator() {
        return treeTraverser.postOrderTraversal(rootNode().get()).iterator();
    }

    static class Node<T> implements TwoDimTree.Node<T> {

        private final double x;
        private final double y;
        private final T input;
        private Optional<TwoDimTree.Node<T>> leftOptional;
        private Optional<TwoDimTree.Node<T>> rightOptional;

        public Node(final double x, final double y, final T input,
                    final Optional<TwoDimTree.Node<T>> leftOptional,
                    final Optional<TwoDimTree.Node<T>> rightOptional) {
            this.x = x;
            this.y = y;
            this.input = input;
            this.leftOptional = checkNotNull(leftOptional);
            this.rightOptional = checkNotNull(rightOptional);
        }

        @Override
        public double xCoordinate() {
            return x;
        }

        @Override
        public double yCoordinate() {
            return y;
        }

        @Override
        public int dimensions() {
            return 2;
        }

        @Override
        public Optional<TwoDimTree.Node<T>> leftChild() {
            return leftOptional;
        }

        @Override
        public Optional<TwoDimTree.Node<T>> rightChild() {
            return rightOptional;
        }

        @Override
        public Iterable<TwoDimTree.Node<T>> children() {
            return Iterables.filter(Arrays.asList(leftChild().orNull(), rightChild().orNull()), Predicates.notNull());
        }

        @Override
        public T value() {
            return input;
        }

        @Override
        public double[] coordinates() {
            return new double[]{x, y};
        }

        @Override
        public double distance(final double... coordinates) {
            checkArgument(coordinates.length == dimensions());
            return Geometry2D.distance(x, y, coordinates[0], coordinates[1]);
        }
    }
}
