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
                final TwoDimTree.Node<T> rightChild = null;
                final double x = point2D.getX();
                final double y = point2D.getY();

                return new SimpleTwoDimTree.Node<T>(x, y, input, Optional.fromNullable(leftChild), Optional.fromNullable(rightChild));
        }

    }

    public static <T> TwoDimTreeFactory<T> newInstance() {
        return new SimpleTwoDimTreeFactory<>();
    }

}
