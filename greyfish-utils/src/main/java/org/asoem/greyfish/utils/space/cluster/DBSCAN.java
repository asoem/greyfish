package org.asoem.greyfish.utils.space.cluster;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.asoem.greyfish.utils.space.SpatialObject;

import javax.annotation.Nullable;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Density-Based Spatial Clustering of Applications with Noise. <p/> <p>The algorithm clusters a set of objects by their
 * density, which is defined by a the reachability distance parameter {@code epsilon}. A second parameter, the minimal
 * neighbourhood size ({@code minPts}), controls whether an object is a core object or just 'density reachable' by
 * others. Objects which are not density reachable get characterized as noise.</p>
 * <p/>
 * <i>The algorithm is deterministic for the number of clusters and their core objects, but not for the cluster
 * assignment of only 'density reachable' objects.</i>
 *
 * @param <O> The type of the spatial objects to cluster
 */
public final class DBSCAN<O extends SpatialObject> {

    /**
     * Maximum radius of the neighborhood to be considered.
     */
    private final double eps;

    /**
     * Minimum number of points needed for a cluster.
     */
    private final int minPts;

    public DBSCAN(final double epsilon, final int minPts) {
        checkArgument(epsilon >= 0.0d, "epsilon must be positive");
        checkArgument(minPts >= 0, "minPts must be positive");

        this.eps = epsilon;
        this.minPts = minPts;
    }

    public DBSCANClusterSet<O> cluster(final Set<O> objects) {
        checkNotNull(objects);

        final List<SpatialCluster<O>> clusters = Lists.newArrayList();
        final List<O> noise = Lists.newArrayList();
        final Map<O, PointStatus> visited = Maps.newHashMap(Maps.asMap(objects, new Function<O, PointStatus>() {
            @Nullable
            @Override
            public PointStatus apply(@Nullable final O input) {
                return PointStatus.UNKNOWN;
            }
        }));

        for (Map.Entry<O, PointStatus> entry : visited.entrySet()) {
            if (entry.getValue().equals(PointStatus.UNKNOWN)) {
                final Optional<SpatialCluster<O>> clusterOptional =
                        createCluster(entry.getKey(), objects, visited);

                if (clusterOptional.isPresent()) {
                    clusters.add(clusterOptional.get());
                } else {
                    entry.setValue(PointStatus.NOISE);
                    noise.add(entry.getKey());
                }
            }
            assert !entry.getValue().equals(PointStatus.UNKNOWN);
        }

        return DBSCANClusterSet.create(clusters, noise, eps, minPts);
    }

    private Optional<SpatialCluster<O>> createCluster(
            final O point,
            final Set<O> points,
            final Map<O, PointStatus> visited) {
        final Set<O> neighbors = filterNeighbors(points, point, eps);
        final List<O> cluster = new ArrayList<>();
        cluster.add(point);
        visited.put(point, PointStatus.PART_OF_CLUSTER);

        Set<O> seeds = ImmutableSet.copyOf(neighbors);
        Optional<O> current;

        do {
            current = Iterables.tryFind(seeds, new Predicate<O>() {
                @Override
                public boolean apply(@Nullable final O input) {
                    final PointStatus status = visited.get(input);
                    return status.equals(PointStatus.UNKNOWN);
                }
            });

            if (current.isPresent()) {
                final Set<O> currentNeighbors = filterNeighbors(points, current.get(), eps);
                if (currentNeighbors.size() >= minPts) {
                    seeds = Sets.union(seeds, currentNeighbors);
                }

                cluster.add(current.get());
                visited.put(current.get(), PointStatus.PART_OF_CLUSTER);
            }
        } while (current.isPresent());

        if (cluster.size() >= minPts) {
            return Optional.of(SpatialCluster.create(cluster));
        } else {
            return Optional.absent();
        }
    }

    private static <O extends SpatialObject> Set<O> filterNeighbors(
            final Set<O> points, final O point, final double eps) {
        return Sets.filter(points, new Predicate<O>() {
            @Override
            public boolean apply(@Nullable final O input) {
                checkNotNull(input);
                return input != point && point.getCentroid().distance(input.getCentroid()) <= eps;
            }
        });
    }

    private enum PointStatus {
        UNKNOWN,
        /**
         * The point has is considered to be noise.
         */
        NOISE,
        /**
         * The point is already part of a cluster.
         */
        PART_OF_CLUSTER
    }

    public static final class DBSCANClusterSet<O extends SpatialObject>
            extends ForwardingSet<SpatialCluster<O>>
            implements ClusterSet<SpatialCluster<O>> {

        private final Set<SpatialCluster<O>> clusters;
        private final double eps;
        private final int minPts;
        private final List<O> noise;

        public DBSCANClusterSet(final Collection<? extends SpatialCluster<O>> clusters,
                                final Collection<? extends O> noise, final double eps, final int minPts) {
            this.noise = ImmutableList.copyOf(noise);
            this.clusters = ImmutableSet.copyOf(clusters);
            this.eps = eps;
            this.minPts = minPts;
        }

        @Override
        protected Set<SpatialCluster<O>> delegate() {
            return clusters;
        }

        public static <O extends SpatialObject> DBSCANClusterSet<O> create(
                final Collection<? extends SpatialCluster<O>> clusters, final List<? extends O> noise,
                final double eps, final int minPts) {
            return new DBSCANClusterSet<>(clusters, noise, eps, minPts);
        }

        // public void add(O el) { ... }
        // This is not a trivial task. Clusters might become mergeable if a new object
        // becomes a core object or, as a new 'only density reachable' object,
        // turns other 'only density reachable' neighbors into core objects.
        // It might be less complex to just rerun the cluster algorithm including the new object.

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            final DBSCANClusterSet that = (DBSCANClusterSet) o;

            if (Double.compare(that.eps, eps) != 0) {
                return false;
            }
            if (minPts != that.minPts) {
                return false;
            }
            if (!clusters.equals(that.clusters)) {
                return false;
            }
            if (!noise.equals(that.noise)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            long temp;
            result = 31 * result + clusters.hashCode();
            temp = Double.doubleToLongBits(eps);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            result = 31 * result + minPts;
            result = 31 * result + noise.hashCode();
            return result;
        }

        public List<O> getNoise() {
            return noise;
        }

        public double getEps() {
            return eps;
        }

        public int getMinPts() {
            return minPts;
        }
    }

}
