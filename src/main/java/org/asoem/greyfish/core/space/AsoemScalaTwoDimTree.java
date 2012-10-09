package org.asoem.greyfish.core.space;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.utils.space.TwoDimTree;
import org.asoem.kdtree.HyperPoint;
import org.asoem.kdtree.HyperPoint2;
import org.asoem.kdtree.KDTree;
import org.asoem.kdtree.NNResult;
import scala.Product2;
import scala.Tuple2;

import static scala.collection.JavaConversions.asJavaIterable;
import static scala.collection.JavaConversions.iterableAsScalaIterable;

public class AsoemScalaTwoDimTree<T> implements TwoDimTree<T> {

    private final static int DIMENSIONS = 2;

    private final KDTree<T> tree;

    private final Function<NNResult<T>,T> neighbourSearchResultTransformation = new Function<NNResult<T>, T>() {
        @Override
        public T apply(NNResult<T> o) {
            return o.value();
        }
    };

    private AsoemScalaTwoDimTree(
            Iterable<? extends T> elements,
            final Function<? super T, ? extends org.asoem.greyfish.utils.collect.Product2<Double, Double>> mappingFunction) {
        final Iterable<Product2<HyperPoint, T>> transform = Iterables.transform(elements, new Function<T, Product2<HyperPoint, T>>() {
            @Override
            public Product2<HyperPoint, T> apply(T t) {
                final org.asoem.greyfish.utils.collect.Product2<Double, Double> b = mappingFunction.apply(t);
                assert b != null;
                return new Tuple2<HyperPoint, T>(new HyperPoint2(b._1(), b._2()), t);
            }
        });
        this.tree = org.asoem.kdtree.KDTree.apply(DIMENSIONS, iterableAsScalaIterable(transform).toList());
    }

    @Override
    public Iterable<T> findObjects(double x, double y, double range) {
        switch (tree.size()) {
            case 0:
                return ImmutableList.of();
            default:
                final HyperPoint searchPoint = new HyperPoint2(x, y);
                final scala.collection.immutable.List<NNResult<T>> nnResultList
                        = tree.filterRange(searchPoint, range);
                return Iterables.transform(asJavaIterable(nnResultList), neighbourSearchResultTransformation);
        }
    }

    public static <T> AsoemScalaTwoDimTree<T> of() {
        final ImmutableMap<T, org.asoem.greyfish.utils.collect.Product2<Double,Double>> map = ImmutableMap.of();
        return new AsoemScalaTwoDimTree<T>(map.keySet(), Functions.forMap(map));
    }

    public static <T> AsoemScalaTwoDimTree<T> of(
            Iterable<? extends T> elements,
            final Function<? super T, ? extends org.asoem.greyfish.utils.collect.Product2<Double, Double>> mappingFunction) {
        return new AsoemScalaTwoDimTree<T>(elements, mappingFunction);
    }
}
