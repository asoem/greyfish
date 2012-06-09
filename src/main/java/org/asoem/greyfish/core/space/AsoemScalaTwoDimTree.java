package org.asoem.greyfish.core.space;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.utils.space.Location2D;
import org.asoem.greyfish.utils.space.TwoDimTree;
import org.asoem.kdtree.HyperPoint;
import org.asoem.kdtree.HyperPoint2;
import org.asoem.kdtree.KDTree;
import org.asoem.kdtree.NNResult;
import scala.Product2;
import scala.Tuple2;

import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;
import static scala.collection.JavaConversions.asJavaIterable;
import static scala.collection.JavaConversions.iterableAsScalaIterable;

public final class AsoemScalaTwoDimTree<T> implements TwoDimTree<T> {

    private KDTree<T> kdtree = KDTree.empty(2);

    private AsoemScalaTwoDimTree() {
    }

    public static <T> AsoemScalaTwoDimTree<T> newInstance() {
        return new AsoemScalaTwoDimTree<T>();
    }

    @Override
    public void rebuild(Iterable<? extends T> elements, final Function<? super T, ? extends Location2D> coordinates2DFunction) {
        final Iterable<Product2<HyperPoint, T>> transform = Iterables.transform(elements, new Function<T, Product2<HyperPoint, T>>() {
            @Override
            public Product2<HyperPoint, T> apply(T t) {
                final Location2D b = coordinates2DFunction.apply(t);
                return new Tuple2<HyperPoint, T>(new HyperPoint2(b.getX(), b.getY()), t);
            }
        });
        kdtree = org.asoem.kdtree.KDTree.apply(2, iterableAsScalaIterable(transform).toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<T> findObjects(Location2D p, double range) {
        checkNotNull(p);

        if (kdtree.size() == 0)
            return Collections.emptyList();

        final HyperPoint searchPoint = new HyperPoint2(p.getX(), p.getY());

        final scala.collection.immutable.List<NNResult<T>> nnResultList
                = kdtree.filterRange(searchPoint, range);

        return Iterables.transform(asJavaIterable(nnResultList), new Function<NNResult<T>, T>() {
            @Override
            public T apply(NNResult<T> o) {
                return o.value();
            }
        });
    }
}
