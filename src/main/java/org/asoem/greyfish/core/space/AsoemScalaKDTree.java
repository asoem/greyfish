package org.asoem.greyfish.core.space;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.kdtree.HyperPoint;
import org.asoem.kdtree.NNResult;
import scala.Tuple2;

import java.util.ArrayList;
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
        List<Tuple2<HyperPoint, T>> pointList = new ArrayList<Tuple2<HyperPoint, T>>();
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
        ArrayList<T> found = new ArrayList<T>();

			long start = 0;
			if (GreyfishLogger.isTraceEnabled())
				start = System.nanoTime();

            HyperPoint searchPoint = new HyperPoint(Arrays.asList(p.getX(), p.getY()));
            scala.collection.immutable.List<NNResult<T>> l
                    = (scala.collection.immutable.List<NNResult<T>>) kdtree.findNeighbours(searchPoint, Integer.MAX_VALUE, range);
            Iterable<NNResult<T>> resultList = scala.collection.JavaConversions.asJavaIterable(l);
            for (NNResult<T> result : resultList) {
                found.add((T)result.value());
            }

			if (GreyfishLogger.isTraceEnabled()) {
				long end = System.nanoTime();
				GreyfishLogger.trace(AsoemScalaKDTree.class.getSimpleName() +"#findNeighbours(): " +
                        "Found " + Iterables.size(found) + " Neighbours in " + (end - start) / 1000 + "us");
			}

		return found;
    }
}
