package org.asoem.greyfish.impl.space;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.utils.space.Geometry2D;
import org.asoem.greyfish.utils.space.ImmutablePoint2D;
import org.asoem.greyfish.utils.space.Point2D;
import org.asoem.greyfish.utils.space.TwoDimTree;
import org.asoem.kdtree.*;
import scala.Product2;
import scala.Tuple2;
import scala.collection.immutable.List;

import javax.annotation.Nullable;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;
import static scala.collection.JavaConversions.asJavaIterable;
import static scala.collection.JavaConversions.iterableAsScalaIterable;

/**
 * This implementation delegates to an instance of a {@link KDTree}.
 *
 * @param <T> the type of the value stored with each node
 */
public class AsoemScalaTwoDimTree<T> implements TwoDimTree<T> {

    private static final int DIMENSIONS = 2;

    private final KDTree<T> tree;

    private final Function<NNResult<T>, SearchResult<T>> neighbourSearchResultTransformation = new Function<NNResult<T>, SearchResult<T>>() {
        @Override
        public SearchResult<T> apply(final NNResult<T> o) {
            return new SearchResult<T>() {
                @Override
                public Node<T> node() {
                    return asTreeNode(o.node());
                }

                @Override
                public double distance() {
                    return o.distance();
                }
            };
        }
    };

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
    public Iterable<SearchResult<T>> findNodes(final double x, final double y, final double range) {
        switch (tree.size()) {
            case 0:
                return ImmutableList.of();
            default:
                final HyperPoint searchPoint = new HyperPoint2(x, y);
                final List<NNResult<T>> nnResultList
                        = tree.filterRange(searchPoint, range);
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
    @Nullable
    public Node<T> root() {
        final KDNode<T> root = tree.root();
        return asTreeNode(root);
    }

    @Override
    public Optional<Node<T>> rootNode() {
        return Optional.fromNullable(asTreeNode(tree.root()));
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

    private static <T> Node<T> createNode(final Node<T> left, final Node<T> right, final T value, final Point2D point) {
        return new Node<T>() {

            @Override
            public Iterable<Node<T>> children() {
                return Iterables.filter(Arrays.asList(left, right), Predicates.notNull());
            }

            @Override
            public T value() {
                return value;
            }

            @Override
            public double distance(final double... coordinates) {
                checkArgument(coordinates.length == dimensions());
                return Geometry2D.distance(xCoordinate(), yCoordinate(), coordinates[0], coordinates[1]);
            }

            @Nullable
            @Override
            public Node<T> leftChild() {
                return left;
            }

            @Nullable
            @Override
            public Node<T> rightChild() {
                return right;
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
        };
    }

    public static <T> AsoemScalaTwoDimTree<T> of() {
        final ImmutableMap<T, org.asoem.greyfish.utils.collect.Product2<Double, Double>> map = ImmutableMap.of();
        return new AsoemScalaTwoDimTree<T>(map.keySet(), Functions.forMap(map));
    }

    public static <T> AsoemScalaTwoDimTree<T> of(
            final Iterable<? extends T> elements,
            final Function<? super T, ? extends org.asoem.greyfish.utils.collect.Product2<Double, Double>> mappingFunction) {
        return new AsoemScalaTwoDimTree<T>(elements, mappingFunction);
    }
}
