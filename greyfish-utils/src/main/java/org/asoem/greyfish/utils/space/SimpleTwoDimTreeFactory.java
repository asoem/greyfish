package org.asoem.greyfish.utils.space;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
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
                final TwoDimTree.Node<T> rightChild = null;
                final double x = point2D.getX();
                final double y = point2D.getY();

                return new Node<T>(x, y, leftChild, rightChild, input);
        }

    }

    public static <T> TwoDimTreeFactory<T> newInstance() {
        return new SimpleTwoDimTreeFactory<T>();
    }

    private class Node<T> implements TwoDimTree.Node<T> {

        private final double x;
        private final double y;
        private final TwoDimTree.Node<T> leftChild;
        private final TwoDimTree.Node<T> rightChild;
        private final T input;

        public Node(final double x, final double y, final TwoDimTree.Node<T> leftChild, final TwoDimTree.Node<T> rightChild, final T input) {
            this.x = x;
            this.y = y;
            this.leftChild = leftChild;
            this.rightChild = rightChild;
            this.input = input;
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

        @Nullable
        @Override
        public TwoDimTree.Node<T> leftChild() {
            return leftChild;
        }

        @Nullable
        @Override
        public TwoDimTree.Node<T> rightChild() {
            return rightChild;
        }

        @Override
        public Iterable<TwoDimTree.Node<T>> children() {
            return Iterables.filter(Arrays.asList(leftChild(), rightChild()), Predicates.notNull());
        }

        @Override
        public T value() {
            return input;
        }

        @Override
        public double distance(final double... coordinates) {
            checkArgument(coordinates.length == dimensions());
            return Geometry2D.distance(x, y, coordinates[0], coordinates[1]);
        }
    }
}
