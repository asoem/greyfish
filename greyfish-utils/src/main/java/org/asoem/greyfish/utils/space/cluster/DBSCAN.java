package org.asoem.greyfish.utils.space.cluster;

import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.asoem.greyfish.utils.space.DistanceMeasure;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.*;

/**
 * Density-Based Spatial Clustering of Applications with Noise.
 * <p/>
 * The algorithm clusters a set of objects by their density, which is defined by a the reachability distance parameter
 * {@code epsilon}. A second parameter, the minimal neighbourhood size ({@code minPts}), controls whether an object is a
 * core object or just 'density reachable' by others. Objects which are not density reachable get characterized as
 * noise.
 * <p/>
 * <i>The algorithm is deterministic for the number of clusters and their core objects, but not for the cluster
 * assignment of only 'density reachable' objects.</i>
 *
 * @param <O> The type of the spatial objects to cluster
 */
public final class DBSCAN<O> implements ClusterAlgorithm<O, DBSCANResult<O>> {

    /**
     * Maximum radius of the neighborhood to be considered.
     */
    private final double epsilon;

    /**
     * Minimum number of points needed for a cluster.
     */
    private final int minPts;

    /**
     * The distance measure to use
     */
    private final DistanceMeasure<? super O> distanceMeasure;

    public DBSCAN(final double epsilon, final int minPts, final DistanceMeasure<? super O> distanceMeasure) {
        checkArgument(epsilon >= 0.0d, "epsilon must be positive");
        checkArgument(minPts >= 0, "minPts must be positive");
        checkNotNull(distanceMeasure);

        this.distanceMeasure = distanceMeasure;
        this.epsilon = epsilon;
        this.minPts = minPts;
    }

    @Override
    public DBSCANResult<O> apply(final Set<? extends O> objects) {
        checkNotNull(objects);

        final List<DBSCANCluster<O>> clusters = Lists.newArrayList();
        final List<O> noise = Lists.newArrayList();
        final Map<O, PointStatus> objectStatusMap =
                Maps.newHashMap(Maps.asMap(objects, Functions.constant(PointStatus.UNKNOWN)));

        for (Map.Entry<O, PointStatus> entry : objectStatusMap.entrySet()) {
            if (entry.getValue().equals(PointStatus.UNKNOWN)) {
                final Optional<DBSCANCluster<O>> clusterOptional =
                        tryCluster(entry.getKey(), objectStatusMap);

                if (clusterOptional.isPresent()) {
                    clusters.add(clusterOptional.get());
                } else {
                    noise.add(entry.getKey());
                }
            }
            assert !entry.getValue().equals(PointStatus.UNKNOWN);
        }

        return DBSCANResult.create(clusters, noise, epsilon, minPts);
    }

    private Optional<DBSCANCluster<O>> tryCluster(final O origin, final Map<O, PointStatus> objectStatusMap) {
        final Set<O> candidates = objectStatusMap.keySet();
        final List<O> clusterObjects = new ArrayList<>();
        clusterObjects.add(origin);

        Set<O> seeds = filterNeighbors(candidates, origin, epsilon, distanceMeasure);

        if (seeds.size() < minPts) {
            objectStatusMap.put(origin, PointStatus.NOISE);
            return Optional.absent();
        } else {
            objectStatusMap.put(origin, PointStatus.CORE_OBJECT);
        }

        Optional<O> seed;

        do {
            seed = Iterables.tryFind(seeds, new Predicate<O>() {
                @Override
                public boolean apply(@Nullable final O input) {
                    final PointStatus status = objectStatusMap.get(input);
                    return status.equals(PointStatus.UNKNOWN);
                }
            });

            if (seed.isPresent()) {
                final Set<O> currentNeighbors = filterNeighbors(candidates, seed.get(), epsilon, distanceMeasure);
                if (currentNeighbors.size() >= minPts) {
                    seeds = Sets.union(seeds, currentNeighbors);
                    objectStatusMap.put(seed.get(), PointStatus.CORE_OBJECT);
                } else {
                    objectStatusMap.put(seed.get(), PointStatus.DENSITY_REACHABLE);
                }

                clusterObjects.add(seed.get());
            }
        } while (seed.isPresent());

        return Optional.of(DBSCANCluster.create(clusterObjects));
    }

    private static <O> Set<O> filterNeighbors(
            final Set<O> points, final O point, final double eps, final DistanceMeasure<? super O> distanceMeasure) {
        return Sets.filter(points, new Predicate<O>() {
            @Override
            public boolean apply(@Nullable final O input) {
                checkNotNull(input);
                if (input == point) {
                    return false;
                }

                final double distance = distanceMeasure.apply(point, input);
                checkState(distance >= 0, "the distance must be positive");

                return distance <= eps;
            }
        });
    }

    private enum PointStatus {
        UNKNOWN,
        NOISE,
        CORE_OBJECT,
        DENSITY_REACHABLE
    }

}
