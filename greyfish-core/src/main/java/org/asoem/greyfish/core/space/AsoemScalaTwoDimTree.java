package org.asoem.greyfish.core.space;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import org.asoem.greyfish.utils.space.ImmutablePoint2D;
import org.asoem.greyfish.utils.space.Point2D;
import org.asoem.greyfish.utils.space.TwoDimTree;
import org.asoem.kdtree.*;
import scala.Product2;
import scala.Tuple2;
import scala.collection.immutable.List;

import javax.annotation.Nullable;

import static scala.collection.JavaConversions.asJavaIterable;
import static scala.collection.JavaConversions.iterableAsScalaIterable;

public class AsoemScalaTwoDimTree<T> implements TwoDimTree<Point2D, T> {

    private final static int DIMENSIONS = 2;

    private final KDTree<T> tree;

    private final Function<NNResult<T>,T> neighbourSearchResultTransformation = new Function<NNResult<T>, T>() {
        @Override
        public T apply(final NNResult<T> o) {
            return o.value();
        }
    };

    private AsoemScalaTwoDimTree(
            final Iterable<? extends T> elements,
            final Function<? super T, ? extends org.asoem.greyfish.utils.collect.Product2<Double, Double>> mappingFunction) {
        final Iterable<Product2<HyperPoint, T>> transform = Iterables.transform(elements, new Function<T, Product2<HyperPoint, T>>() {
            @Override
            public Product2<HyperPoint, T> apply(final T t) {
                final org.asoem.greyfish.utils.collect.Product2<Double, Double> b = mappingFunction.apply(t);
                assert b != null;
                return new Tuple2<HyperPoint, T>(new HyperPoint2(b._1(), b._2()), t);
            }
        });
        this.tree = KDTree.apply(DIMENSIONS, iterableAsScalaIterable(transform).toList());
    }

    @Override
    public Iterable<T> findObjects(final double x, final double y, final double range) {
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
    public Iterable<T> findObjects(final Point2D center, final double range) {
        return findObjects(center.getX(), center.getY(), range);
    }

    @Override
    public int size() {
        return tree.size();
    }

    @Override
    @Nullable
    public org.asoem.greyfish.utils.space.KDNode<Point2D, T> root() {
        final KDNode<T> root = tree.root();
        return root == null ? null : asTreeNode(root);
    }

    private org.asoem.greyfish.utils.space.KDNode<Point2D, T> asTreeNode(final KDNode<T> node) {
        assert node != null;
        return new org.asoem.greyfish.utils.space.KDNode<Point2D, T>() {
            final Iterable<org.asoem.greyfish.utils.space.KDNode<Point2D, T>> children =
                    ImmutableList.copyOf(Iterators.transform(Iterators.filter(Iterators.forArray(node.left(), node.right()), Predicates.notNull()),
                            new Function<KDNode<T>, org.asoem.greyfish.utils.space.KDNode<Point2D, T>>() {
                                @Nullable
                                @Override
                                public org.asoem.greyfish.utils.space.KDNode<Point2D, T> apply(@Nullable final KDNode<T> input) {
                                    return asTreeNode(input);
                                }
                            }));
            private final T value = node.value();
            private final Point2D point = ImmutablePoint2D.at(node.point().apply(0), node.point().apply(1));

            @Override
            public Iterable<org.asoem.greyfish.utils.space.KDNode<Point2D, T>> children() {
                return children;
            }

            @Override
            public Point2D point() {
                return point;
            }

            @Override
            public T value() {
                return value;
            }
        };
    }

    public static <T> AsoemScalaTwoDimTree<T> of() {
        final ImmutableMap<T, org.asoem.greyfish.utils.collect.Product2<Double,Double>> map = ImmutableMap.of();
        return new AsoemScalaTwoDimTree<T>(map.keySet(), Functions.forMap(map));
    }

    public static <T> AsoemScalaTwoDimTree<T> of(
            final Iterable<? extends T> elements,
            final Function<? super T, ? extends org.asoem.greyfish.utils.collect.Product2<Double, Double>> mappingFunction) {
        return new AsoemScalaTwoDimTree<T>(elements, mappingFunction);
    }
}
