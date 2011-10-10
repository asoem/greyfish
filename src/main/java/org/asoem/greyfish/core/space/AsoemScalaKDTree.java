package org.asoem.greyfish.core.space;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.kdtree.HyperPoint;
import org.asoem.kdtree.HyperPoint2;
import org.asoem.kdtree.KDTuple;
import org.asoem.kdtree.NNResult;

import static com.google.common.base.Preconditions.checkNotNull;
import static scala.collection.JavaConversions.asJavaIterable;
import static scala.collection.JavaConversions.iterableAsScalaIterable;

public final class AsoemScalaKDTree<T extends Object2D> implements KDTree<T> {

    private org.asoem.kdtree.KDTree<T> kdtree =
            new org.asoem.kdtree.KDTree<T>(iterableAsScalaIterable(ImmutableList.<KDTuple<T>>of()));

    private AsoemScalaKDTree() {
    }

    public static <T extends Object2D> AsoemScalaKDTree<T> newInstance() {
        return new AsoemScalaKDTree<T>();
    }

    @Override
    public void rebuild(Iterable<? extends T> elements) {
        kdtree = new org.asoem.kdtree.KDTree<T>(iterableAsScalaIterable(Iterables.transform(elements, new Function<T, KDTuple<T>>() {
            @Override
            public KDTuple<T> apply(T t) {
                final Coordinates2D b = t.getCoordinates();
                return new KDTuple<T>(new HyperPoint2(b.getX(), b.getY()), t);
            }
        })));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<T> findObjects(Coordinates2D p, double range) {
        checkNotNull(p);

        HyperPoint searchPoint = new HyperPoint2(p.getX(), p.getY());

        scala.collection.immutable.List<NNResult<T>> nnResultList
                = (scala.collection.immutable.List<NNResult<T>>) kdtree.findNeighbours(searchPoint, Integer.MAX_VALUE, range);

        return Iterables.transform(asJavaIterable(nnResultList), new Function<NNResult<T>, T>() {
            @Override
            public T apply(NNResult<T> o) {
                return (T) o.value();
            }
        });
    }
}
