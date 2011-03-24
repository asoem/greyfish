package org.asoem.greyfish.core.space;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.asoem.kdtree.HyperPoint;
import org.asoem.kdtree.HyperPoint2;
import org.asoem.kdtree.KDTuple;
import org.asoem.kdtree.NNResult;

import java.util.Arrays;

import static scala.collection.JavaConversions.asJavaIterable;
import static scala.collection.JavaConversions.asScalaIterable;

public final class AsoemScalaKDTree<T extends Object2D> implements KDTree<T> {

    private org.asoem.kdtree.KDTree kdtree = new org.asoem.kdtree.KDTree();

    private AsoemScalaKDTree() {
    }

    public static <T extends Object2D> AsoemScalaKDTree<T> newInstance() {
        return new AsoemScalaKDTree<T>();
    }

    @Override
    public void rebuild(Iterable<T> elements) {
        kdtree = new org.asoem.kdtree.KDTree(asScalaIterable(Iterables.transform(elements, new Function<T, Object>() {
            @Override
            public KDTuple apply(T t) {
                final Location2D b = t.getAnchorPoint();
                return new KDTuple(new HyperPoint2(b.getX(), b.getY()), t);
            }
        })));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<T> findNeighbours(Location2D p, double range) {

        final HyperPoint searchPoint = new HyperPoint2(p.getX(), p.getY());

        final scala.collection.immutable.List<NNResult<T>> nnResultList
                = (scala.collection.immutable.List<NNResult<T>>) kdtree.findNeighbours(searchPoint, Integer.MAX_VALUE, range);

        return Iterables.transform(asJavaIterable(nnResultList), new Function<NNResult<T>, T>() {
            @Override
            public T apply(NNResult<T> o) {
                return (T) o.value();
            }
        });
    }
}
