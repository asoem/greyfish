package org.asoem.greyfish.impl.space;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.BinaryTreeTraverser;
import com.google.common.collect.Iterables;
import com.google.common.collect.TreeTraverser;
import com.google.common.primitives.Doubles;
import org.asoem.greyfish.utils.collect.AbstractTree;
import org.asoem.greyfish.utils.space.DistanceMeasures;
import org.asoem.greyfish.utils.space.DistantObject;
import org.asoem.greyfish.utils.space.KDTree;
import org.asoem.greyfish.utils.space.Point;
import org.asoem.kdtree.*;
import scala.Product2;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static scala.collection.JavaConversions.*;

public final class ASKDTree<T> extends AbstractTree<ASKDTree.KDNodeWrapper<T>>
        implements KDTree<ASKDTree.KDNodeWrapper<T>> {

    private final org.asoem.kdtree.KDTree<T> tree;

    public ASKDTree(final int dimensions, final Map<T, ? extends Point> elements) {
        checkNotNull(elements);

        this.tree = buildTree(elements, dimensions);
    }

    private org.asoem.kdtree.KDTree<T> buildTree(final Map<T, ? extends Point> elements, final int dimensions) {

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
    protected TreeTraverser<ASKDTree.KDNodeWrapper<T>> getTreeTraverser() {
        return new Traverser<>();
    }

    @Override
    public int dimensions() {
        return tree.dim();
    }

    @Override
    public Iterable<DistantObject<KDNodeWrapper<T>>> rangeSearch(final double[] center, final double range) {
        return Iterables.transform(
                asJavaIterable(tree.filterRange(HyperSphere$.MODULE$.apply(HyperPoint$.MODULE$.apply(center), range))),
                new Function<NNResult<T>, DistantObject<KDNodeWrapper<T>>>() {
                    @Nullable
                    @Override
                    public DistantObject<KDNodeWrapper<T>> apply(final NNResult<T> input) {
                        return new DistantObject<KDNodeWrapper<T>>() {
                            @Override
                            public KDNodeWrapper<T> object() {
                                return new KDNodeWrapper<>(input.node());
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
    public Optional<ASKDTree.KDNodeWrapper<T>> rootNode() {
        return (tree.root().isDefined())
                ? Optional.of(new KDNodeWrapper<>(tree.root().get()))
                : Optional.<KDNodeWrapper<T>>absent();
    }

    private static class Traverser<N extends org.asoem.greyfish.utils.space.KDNode<N, ?>>
            extends BinaryTreeTraverser<N> {
        @Override
        public Optional<N> leftChild(final N root) {
            return root.leftChild();
        }

        @Override
        public Optional<N> rightChild(final N root) {
            return root.rightChild();
        }
    }

    public static final class KDNodeWrapper<T> implements org.asoem.greyfish.utils.space.KDNode<KDNodeWrapper<T>, T> {
        private final org.asoem.kdtree.KDNode<T> node;

        public KDNodeWrapper(final org.asoem.kdtree.KDNode<T> node) {
            this.node = node;
        }

        @Override
        public int dimensions() {
            return node.dim();
        }

        @Override
        public T value() {
            return node.value();
        }

        @Override
        public double[] coordinates() {
            return Doubles.toArray((List<Double>) (List) asJavaList(node._1().coordinates()));
        }

        @Override
        public double distance(final double... coordinates) {
            return DistanceMeasures.euclidean().apply(coordinates(), coordinates);
        }

        @Override
        public Optional<KDNodeWrapper<T>> leftChild() {
            return node.leftChild().isDefined()
                    ? Optional.of(new KDNodeWrapper<>(node.leftChild().get()))
                    : Optional.<KDNodeWrapper<T>>absent();
        }

        @Override
        public Optional<KDNodeWrapper<T>> rightChild() {
            return node.rightChild().isDefined()
                    ? Optional.of(new KDNodeWrapper<>(node.rightChild().get()))
                    : Optional.<KDNodeWrapper<T>>absent();
        }

        @Override
        public Iterable<KDNodeWrapper<T>> children() {
            return Iterables.transform(asJavaIterable(node.children()),
                    new Function<org.asoem.kdtree.KDNode<T>, KDNodeWrapper<T>>() {
                        @Nullable
                        @Override
                        public KDNodeWrapper<T> apply(@Nullable final KDNode<T> input) {
                            return new KDNodeWrapper<T>(input);
                        }
                    });
        }
    }
}
