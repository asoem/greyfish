package org.asoem.greyfish.utils.space.cluster;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.utils.space.Point;
import org.asoem.greyfish.utils.space.SpatialObject;

import java.util.Set;

public final class SpatialCluster<O extends SpatialObject>
        extends ForwardingSet<O>
        implements Cluster<O> {

    private final ImmutableSet<O> objects;

    public SpatialCluster(final ImmutableSet<O> objects) {
        this.objects = objects;
    }

    @Override
    protected Set<O> delegate() {
        return objects;
    }

    public static <O extends SpatialObject> SpatialCluster<O> create(final Iterable<? extends O> cluster) {
        return new SpatialCluster<>(ImmutableSet.copyOf(cluster));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SpatialCluster that = (SpatialCluster) o;

        if (!objects.equals(that.objects)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return objects.hashCode();
    }

    /**
     * Check if the cluster contains an object which has distance to given {@code point} less than or equal to {@code
     * epsilon}.
     *
     * @param point   the point to check for a possible neighborhood
     * @param epsilon the maximum neighborhood distance
     * @return {@code true} if the given {@code point} is located in the {@code epsilon} neighborhood of this cluster,
     * {@code false} otherwise
     */
    public boolean isNeighborhood(final Point point, final double epsilon) {
        return Iterables.any(this, new Predicate<O>() {
            @Override
            public boolean apply(final O input) {
                return input.getCentroid().distance(point) <= epsilon;
            }
        });
    }
}
