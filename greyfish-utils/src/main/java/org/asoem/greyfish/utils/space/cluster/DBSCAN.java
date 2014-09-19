package org.asoem.greyfish.utils.space.cluster;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.asoem.greyfish.utils.space.DistanceMeasure;
import org.asoem.greyfish.utils.space.SpatialData;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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

    private final DistanceMeasure<? super O> distanceMeasure;

    private final NeighborSearch<O> neighborSearch;

    public DBSCAN(final double epsilon, final int minPts, final DistanceMeasure<? super O> distanceMeasure) {
        this(epsilon, minPts, distanceMeasure, new NaiveNeighborSearch<O>());
    }

    public DBSCAN(final double epsilon, final int minPts, final DistanceMeasure<? super O> distanceMeasure,
                  final NeighborSearch<O> neighborSearch) {
        checkArgument(epsilon >= 0.0d, "epsilon must be positive");
        checkArgument(minPts >= 0, "minPts must be positive");
        checkNotNull(distanceMeasure);

        this.distanceMeasure = distanceMeasure;
        this.epsilon = epsilon;
        this.minPts = minPts;
        this.neighborSearch = neighborSearch;
    }

    @Override
    public DBSCANResult<O> apply(final Collection<O> objects) {
        checkNotNull(objects);

        final List<DBSCANCluster<O>> clusters = Lists.newArrayList();
        final List<O> noise = Lists.newArrayList();
        final Map<O, PointStatus> objectStatusMap = Maps.newIdentityHashMap();
        for (O object : objects) {
            objectStatusMap.put(object, PointStatus.UNKNOWN);
        }

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

    private Optional<DBSCANCluster<O>> tryCluster(
            final O origin, final Map<O, PointStatus> objectStatusMap) {
        final List<O> clusterObjects = new ArrayList<>();
        final Queue<O> seeds = Queues.newArrayDeque();

        assert objectStatusMap.get(origin).equals(PointStatus.UNKNOWN);
        objectStatusMap.put(origin, PointStatus.SEED);
        seeds.offer(origin);

        final Set<O> candidates = objectStatusMap.keySet();

        while (!seeds.isEmpty()) {
            final O seed = seeds.poll();
            assert seed != null;

            final Collection<O> currentNeighbors =
                    neighborSearch.filterNeighbors(candidates, seed, distanceMeasure, epsilon);
            if (currentNeighbors.size() >= minPts) {
                for (O neighbor : currentNeighbors) {
                    if (objectStatusMap.get(neighbor).equals(PointStatus.NOISE)) {
                        clusterObjects.add(neighbor);
                        objectStatusMap.put(neighbor, PointStatus.DENSITY_REACHABLE);
                    } else if (objectStatusMap.get(neighbor).equals(PointStatus.UNKNOWN)) {
                        seeds.offer(neighbor);
                        objectStatusMap.put(neighbor, PointStatus.SEED);
                    }
                }
                objectStatusMap.put(seed, PointStatus.CORE_OBJECT);
            } else {
                objectStatusMap.put(seed, PointStatus.DENSITY_REACHABLE);
            }
            clusterObjects.add(seed);
        }

        if (clusterObjects.size() == 1) {
            objectStatusMap.put(origin, PointStatus.NOISE);
            return Optional.absent();
        } else {
            return Optional.of(DBSCANCluster.create(clusterObjects));
        }
    }

    private enum PointStatus {
        UNKNOWN,
        NOISE,
        CORE_OBJECT,
        SEED, DENSITY_REACHABLE
    }

    public static final class NaiveNeighborSearch<O> implements NeighborSearch<O> {

        public NaiveNeighborSearch() {
        }

        @Override
        public Collection<O> filterNeighbors(
                final Collection<O> collection, final O origin,
                final DistanceMeasure<? super O> distanceMeasure, final double range) {
            return SpatialData.filterNeighbors(collection, origin, range, distanceMeasure);
        }
    }
}
