package org.asoem.greyfish.core.space;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.kdtree.HyperPoint;
import org.asoem.kdtree.NNResult;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;

public final class AsoemScalaKDTree<T extends MovingObject2D> implements KDTree<T> {

    private org.asoem.kdtree.KDTree kdtree;

    private AsoemScalaKDTree() {
    }

    public static <T extends MovingObject2D> AsoemScalaKDTree<T> newInstance() {
        return new AsoemScalaKDTree<T>();
    }

    @Override
    public void rebuild(Iterable<T> elements) {
        List<Tuple2<HyperPoint, T>> pointList = Lists.newArrayList();
        for (T element : elements) {
            final Location2D b = element.getAnchorPoint();
            final HyperPoint hp = new HyperPoint(Arrays.asList(b.getX(), b.getY()));
            pointList.add(new Tuple2<HyperPoint, T>(hp, element));
        }
        kdtree = new org.asoem.kdtree.KDTree(pointList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<T> findNeighbours(Location2D p, double range) {

        HyperPoint searchPoint = new HyperPoint(Arrays.asList(p.getX(), p.getY()));

        scala.collection.immutable.List<NNResult<T>> nnResultList
                = (scala.collection.immutable.List<NNResult<T>>) kdtree.findNeighbours(searchPoint, Integer.MAX_VALUE, range);

        return Iterables.transform(scala.collection.JavaConversions.asJavaIterable(nnResultList), new Function<NNResult<T>, T>() {
            @Override
            public T apply(NNResult<T> o) {
                return (T) o.value();
            }
        });
    }
}
