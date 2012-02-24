package org.asoem.greyfish.core.space;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.utils.space.Location2D;
import org.asoem.greyfish.utils.space.TwoDimTree;
import org.asoem.kdtree.*;

import javax.annotation.Nullable;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;
import static scala.collection.JavaConversions.asJavaIterable;
import static scala.collection.JavaConversions.iterableAsScalaIterable;

public final class AsoemScalaTwoDimTree<T> implements TwoDimTree<T> {

    private org.asoem.kdtree.KDTree<T> kdtree =
            new org.asoem.kdtree.KDTree<T>(iterableAsScalaIterable(ImmutableList.<KDTuple<T>>of()));

    private AsoemScalaTwoDimTree() {
    }

    public static <T> AsoemScalaTwoDimTree<T> newInstance() {
        return new AsoemScalaTwoDimTree<T>();
    }

    @Override
    public void rebuild(Iterable<? extends T> elements, final Function<? super T, ? extends Location2D> coordinates2DFunction) {
        kdtree = new org.asoem.kdtree.KDTree<T>(iterableAsScalaIterable(Iterables.transform(elements, new Function<T, KDTuple<T>>() {
            @Override
            public KDTuple<T> apply(T t) {
                final Location2D b = coordinates2DFunction.apply(t);
                return new KDTuple<T>(new HyperPoint2(b.getX(), b.getY()), t);
            }
        })));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<T> findObjects(Location2D p, double range) {
        checkNotNull(p);

        HyperPoint searchPoint = new HyperPoint2(p.getX(), p.getY());

        scala.collection.immutable.List<NNResult<T>> nnResultList
                = kdtree.findNeighbours(searchPoint, Integer.MAX_VALUE, range);

        return Iterables.transform(asJavaIterable(nnResultList), new Function<NNResult<T>, T>() {
            @Override
            public T apply(NNResult<T> o) {
                return o.value();
            }
        });
    }

    @Override
    public Iterator<T> iterator() {
        return Iterables.transform(asJavaIterable(kdtree), new Function<KDNode<T>, T>() {
            @Override
            public T apply(@Nullable KDNode<T> o) {
                assert o != null;
                return o.value();
            }
        }).iterator();
    }
}
