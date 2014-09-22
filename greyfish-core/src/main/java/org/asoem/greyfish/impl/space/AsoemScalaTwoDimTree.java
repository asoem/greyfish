package org.asoem.greyfish.impl.space;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.*;
import org.asoem.greyfish.utils.collect.AbstractTree;
import org.asoem.greyfish.utils.space.*;
import org.asoem.kdtree.*;
import org.asoem.kdtree.KDNode;
import org.asoem.kdtree.KDTree;
import scala.Product2;
import scala.Tuple2;
import scala.collection.immutable.List;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkArgument;
import static scala.collection.JavaConversions.asJavaIterable;
import static scala.collection.JavaConversions.iterableAsScalaIterable;

/**
 * This implementation delegates to an instance of a {@link KDTree}.
 *
 * @param <T> the type of the value stored with each node
 */
public class AsoemScalaTwoDimTree<T> extends AbstractTree<TwoDimTree.Node<T>> implements TwoDimTree<T> {

    private static final int DIMENSIONS = 2;

    private final KDTree<T> tree;

    private final Function<NNResult<T>, DistantObject<TwoDimTree.Node<T>>> neighbourSearchResultTransformation =
            new Function<NNResult<T>, DistantObject<TwoDimTree.Node<T>>>() {
                @Override
                public DistantObject<TwoDimTree.Node<T>> apply(final NNResult<T> o) {
                    return new DistantObject<TwoDimTree.Node<T>>() {
                        @Override
                        public TwoDimTree.Node<T> object() {
                            return asTreeNode(o.node());
                        }

                        @Override
                        public double distance() {
                            return o.distance();
                        }
                    };
                }
            };
    private final BinaryTreeTraverser<Node<T>> treeTraverser = new TwoDimTreeTraverser<>();

    private AsoemScalaTwoDimTree(
            final Iterable<? extends T> elements,
            final Function<? super T, ? extends org.asoem.greyfish.utils.collect.Product2<Double, Double>> mappingFunction) {
        final Iterable<Product2<HyperPoint, T>> transform =
                Iterables.transform(elements, new Function<T, Product2<HyperPoint, T>>() {
                    @Override
                    public Product2<HyperPoint, T> apply(final T t) {
                        final org.asoem.greyfish.utils.collect.Product2<Double, Double> b = mappingFunction.apply(t);
                        assert b != null;
                        return new Tuple2<HyperPoint, T>(new HyperPoint2(b.first(), b.second()), t);
                    }
                });
        this.tree = KDTree.apply(DIMENSIONS, iterableAsScalaIterable(transform).toList());
    }

    @Override
    public Iterable<DistantObject<TwoDimTree.Node<T>>> findNodes(final double x, final double y, final double range) {
        switch (tree.size()) {
            case 0:
                return ImmutableList.of();
            default:
                final HyperPoint searchPoint = new HyperPoint2(x, y);
                final List<NNResult<T>> nnResultList
                        = tree.filterRange(HyperSphere$.MODULE$.apply(searchPoint, range));
                return Iterables.transform(asJavaIterable(nnResultList), neighbourSearchResultTransformation);
        }
    }

    @Override
    public int dimensions() {
        return 2;
    }

    @Override
    public int size() {
        return tree.size();
    }

    @Override
    public Iterable<DistantObject<Node<T>>> rangeSearch(final double[] center, final double range) {
        checkArgument(center.length == dimensions(), "Dimension mismatch");
        return ImmutableList.<DistantObject<Node<T>>>copyOf(findNodes(center[0], center[1], range));
    }

    @Override
    @Nullable
    public Node<T> root() {
        final KDNode<T> root = tree.root().getOrElse(null);
        return asTreeNode(root);
    }

    @Override
    public Optional<Node<T>> rootNode() {
        return Optional.fromNullable(root());
    }

    @Nullable
    private Node<T> asTreeNode(@Nullable final KDNode<T> node) {
        if (node == null) {
            return null;
        } else {

            final Node<T> left = asTreeNode(node.left());
            final Node<T> right = asTreeNode(node.right());
            final T value = node.value();
            final Point2D point = ImmutablePoint2D.at(node.point().apply(0), node.point().apply(1));

            return createNode(left, right, value, point);
        }
    }

    private static <T> Node<T> createNode(@Nullable final Node<T> left, @Nullable final Node<T> right,
                                          final T value, final Point2D point) {
        return new SimpleNode<>(value, point, Optional.fromNullable(left), Optional.fromNullable(right));
    }

    public static <T> AsoemScalaTwoDimTree<T> of() {
        final ImmutableMap<T, org.asoem.greyfish.utils.collect.Product2<Double, Double>> map = ImmutableMap.of();
        return new AsoemScalaTwoDimTree<>(map.keySet(), Functions.forMap(map));
    }

    public static <T> AsoemScalaTwoDimTree<T> of(
            final Iterable<? extends T> elements,
            final Function<? super T, ? extends org.asoem.greyfish.utils.collect.Product2<Double, Double>> mappingFunction) {
        return new AsoemScalaTwoDimTree<>(elements, mappingFunction);
    }

    @Override
    public Iterator<TwoDimTree.Node<T>> iterator() {
        final Optional<Node<T>> rootOptional = rootNode();
        if (rootOptional.isPresent()) {
            return getTreeTraverser().postOrderTraversal(rootOptional.get()).iterator();
        } else {
            return Iterators.emptyIterator();
        }
    }

    @Override
    protected TreeTraverser<Node<T>> getTreeTraverser() {
        return treeTraverser;
    }

    private static class SimpleNode<T> implements Node<T> {

        private final T value;
        private final Point2D point;
        private final Optional<Node<T>> leftOptional;
        private final Optional<Node<T>> rightOptional;

        public SimpleNode(final T value, final Point2D point,
                          final Optional<Node<T>> leftOptional, final Optional<Node<T>> rightOptional) {
            this.value = value;
            this.point = point;
            this.leftOptional = leftOptional;
            this.rightOptional = rightOptional;
        }

        @Override
        public Iterable<Node<T>> children() {
            return Iterables.filter(Arrays.asList(leftChild().orNull(), rightChild().orNull()), Predicates.notNull());
        }

        @Override
        public T value() {
            return value;
        }

        @Override
        public double[] coordinates() {
            return point.coordinates();
        }

        @Override
        public double distance(final double... coordinates) {
            checkArgument(coordinates.length == dimensions());
            return Geometry2D.distance(xCoordinate(), yCoordinate(), coordinates[0], coordinates[1]);
        }

        @Override
        public Optional<Node<T>> leftChild() {
            return leftOptional;
        }

        @Override
        public Optional<Node<T>> rightChild() {
            return rightOptional;
        }

        @Override
        public double xCoordinate() {
            return point.getX();
        }

        @Override
        public double yCoordinate() {
            return point.getY();
        }

        @Override
        public int dimensions() {
            return 2;
        }
    }
}
