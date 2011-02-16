package org.asoem.greyfish.core.space;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.kdtree.HyperPoint;
import org.asoem.kdtree.NNResult;
import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;

public final class AsoemScalaKDTree<T extends Object2DInterface> implements KDTree<T> {

    private org.asoem.kdtree.KDTree kdtree;

    private AsoemScalaKDTree() {
    }

    public static <T extends Object2DInterface> AsoemScalaKDTree<T> newInstance() {
        return new AsoemScalaKDTree<T>();
    }

    @Override
    public void rebuild(Iterable<T> elements) {
        List<Tuple2<HyperPoint, T>> pointList = Lists.newArrayList();
        for (T element : elements) {
            final Location2DInterface b = element.getAnchorPoint();
            final HyperPoint hp = new HyperPoint(Arrays.asList(b.getX(), b.getY()));
            pointList.add(new Tuple2<HyperPoint, T>(hp, element));
        }
        kdtree = new org.asoem.kdtree.KDTree(pointList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<T> findNeighbours(Location2DInterface p, double range) {

        HyperPoint searchPoint = new HyperPoint(Arrays.asList(p.getX(), p.getY()));

        StopWatch stopWatch = null;
        if (GreyfishLogger.LOG4J_LOGGER.logger.isInfoEnabled())
            stopWatch = new LoggingStopWatch("AsoemScalaKDTree:findNeighbours");

        scala.collection.immutable.List<NNResult<T>> nnResultList
                = (scala.collection.immutable.List<NNResult<T>>) kdtree.findNeighbours(searchPoint, Integer.MAX_VALUE, range);

        if (GreyfishLogger.LOG4J_LOGGER.logger.isInfoEnabled()) {
            assert stopWatch != null;
            stopWatch.stop();
        }

        return Iterables.transform(scala.collection.JavaConversions.asJavaIterable(nnResultList), new Function<NNResult<T>, T>() {
            @Override
            public T apply(NNResult<T> o) {
                return (T) o.value();
            }
        });
    }
}
