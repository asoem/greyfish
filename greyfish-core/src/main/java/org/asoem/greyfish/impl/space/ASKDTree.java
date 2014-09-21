package org.asoem.greyfish.impl.space;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.BinaryTreeTraverser;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.TreeTraverser;
import com.google.common.primitives.Doubles;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.asoem.greyfish.utils.collect.AbstractTree;
import org.asoem.greyfish.utils.space.DistantObject;
import org.asoem.greyfish.utils.space.KDNode;
import org.asoem.greyfish.utils.space.KDTree;
import org.asoem.greyfish.utils.space.Point;
import org.asoem.kdtree.HyperPoint;
import org.asoem.kdtree.HyperPoint$;
import org.asoem.kdtree.HyperSphere$;
import org.asoem.kdtree.NNResult;
import scala.Product2;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static scala.collection.JavaConversions.asJavaIterable;
import static scala.collection.JavaConversions.iterableAsScalaIterable;

public final class ASKDTree<T> extends AbstractTree<ASKDTree.ImmutableKDNode<T>>
        implements KDTree<ASKDTree.ImmutableKDNode<T>> {
    private static final EuclideanDistance euclideanDistance = new EuclideanDistance();

    private final int dimensions;
    private final org.asoem.kdtree.KDTree<T> tree;

    public ASKDTree(final int dimensions, final Map<T, ? extends Point> elements) {
        checkNotNull(elements);

        this.dimensions = dimensions;
        this.tree = buildTree(elements);
        assert tree.dim() == dimensions;
    }

    private org.asoem.kdtree.KDTree<T> buildTree(final Map<T, ? extends Point> elements) {

        final Iterable<Product2<HyperPoint, T>> transform = Iterables.transform(elements.entrySet(),
                new Function<Map.Entry<T, ? extends Point>, Product2<HyperPoint, T>>() {
                    @Nullable
                    @Override
                    public Product2<HyperPoint, T> apply(final Map.Entry<T, ? extends Point> input) {
                        return new Tuple2<>(HyperPoint$.MODULE$.apply(input.getValue().coordinates()), input.getKey());
                    }
                });

        return org.asoem.kdtree.KDTree.apply(dimensions, iterableAsScalaIterable(transform).toList());
    }

    @Override
    protected TreeTraverser<ASKDTree.ImmutableKDNode<T>> getTreeTraverser() {
        return new Traverser<>();
    }

    @Override
    public int dimensions() {
        return dimensions;
    }

    @Override
    public Iterable<DistantObject<ImmutableKDNode<T>>> rangeSearch(final double[] center, final double range) {
        return Iterables.transform(
                asJavaIterable(tree.filterRange(HyperSphere$.MODULE$.apply(HyperPoint$.MODULE$.apply(center), range))),
                new Function<NNResult<T>, DistantObject<ImmutableKDNode<T>>>() {
                    @Nullable
                    @Override
                    public DistantObject<ImmutableKDNode<T>> apply(final NNResult<T> input) {
                        return new DistantObject<ImmutableKDNode<T>>() {
                            @Override
                            public ImmutableKDNode<T> object() {
                                return asTreeNode(input.node());
                            }

                            @Override
                            public double distance() {
                                return input.distance();
                            }
                        };
                    }
                });
    }

    @Override
    public Optional<ASKDTree.ImmutableKDNode<T>> rootNode() {
        return Optional.fromNullable(asTreeNode(tree.root()));
    }

    private static class Traverser<N extends KDNode<N, ?>> extends BinaryTreeTraverser<N> {
        @Override
        public Optional<N> leftChild(final N root) {
            return Optional.fromNullable(root.leftChild());
        }

        @Override
        public Optional<N> rightChild(final N root) {
            return Optional.fromNullable(root.rightChild());
        }
    }

    @Nullable
    private ASKDTree.ImmutableKDNode<T> asTreeNode(@Nullable final org.asoem.kdtree.KDNode<T> node) {
        if (node == null) {
            return null;
        } else {

            final ASKDTree.ImmutableKDNode<T> left = asTreeNode(node.left());
            final ASKDTree.ImmutableKDNode<T> right = asTreeNode(node.right());
            final T value = node.value();

            return new ImmutableKDNode<T>(left, right, dimensions, value, asPoint(node.point()));
        }
    }

    private Point asPoint(final HyperPoint point) {
        return new Point() {

            @Override
            public double[] coordinates() {
                return Doubles.toArray(ImmutableList.copyOf(
                        Iterables.transform(asJavaIterable(point.coordinates()),
                                new Function<Object, Double>() {
                                    @Nullable
                                    @Override
                                    public Double apply(@Nullable final Object input) {
                                        return (Double) input;
                                    }
                                })));
            }

            @Override
            public double distance(final Point point) {
                return euclideanDistance.compute(coordinates(), point.coordinates());
            }

            @Override
            public int getDimension() {
                return point.dim();
            }

            @Override
            public Point getCentroid() {
                return this;
            }
        };
    }

    public static class ImmutableKDNode<T> implements KDNode<ImmutableKDNode<T>, T> {

        private final ImmutableKDNode<T> left;
        private final ImmutableKDNode<T> right;
        private final int dimensions;
        private final T value;
        private final Point point;

        private ImmutableKDNode(final ImmutableKDNode<T> left, final ImmutableKDNode<T> right,
                                final int dimensions, final T value, final Point point) {
            this.left = left;
            this.right = right;
            this.dimensions = dimensions;
            this.value = value;
            this.point = point;
        }

        @Override
        public int dimensions() {
            return dimensions;
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
            return euclideanDistance.compute(point.coordinates(), coordinates);
        }

        @Nullable
        @Override
        public ImmutableKDNode<T> leftChild() {
            return left;
        }

        @Nullable
        @Override
        public ImmutableKDNode<T> rightChild() {
            return right;
        }

        @Override
        public Iterable<ImmutableKDNode<T>> children() {
            return Iterables.filter(Arrays.asList(leftChild(), rightChild()), Predicates.notNull());
        }
    }
}
