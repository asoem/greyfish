/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.utils.space.cluster;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.asoem.greyfish.utils.space.DistanceMeasure;
import org.asoem.greyfish.utils.space.Point;
import org.asoem.greyfish.utils.space.Points;
import org.asoem.greyfish.utils.space.SpatialData;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Density-Based Spatial Clustering of Applications with Noise.
 * <p>
 * The algorithm clusters a set of objects by their density, which is defined by a the reachability distance parameter
 * {@code epsilon}. A second parameter, the minimal neighbourhood size ({@code minPts}), controls whether an object is a
 * core object or just 'density reachable' by others. Objects which are not density reachable get characterized as
 * noise.
 * </p>
 * <i>The algorithm is deterministic for the number of clusters and their core objects, but not for the cluster
 * assignment of only 'density reachable' objects.</i>
 *
 * @param <T> The type of the spatial objects to cluster
 */
public final class DBSCAN<T> implements ClusterAlgorithm<T, DBSCANResult<T>> {

    private final double epsilon;

    private final int minPts;

    private final NeighborSearch<T> neighborSearch;

    private DBSCAN(final double epsilon, final int minPts, final NeighborSearch<T> neighborSearch) {
        assert epsilon >= 0.0d;
        assert minPts >= 0;
        assert neighborSearch != null;

        this.epsilon = epsilon;
        this.minPts = minPts;
        this.neighborSearch = neighborSearch;
    }

    public static <T extends Point> DBSCAN<T> create(final double epsilon, final int minPts) {
        return create(epsilon, minPts, Points.euclideanDistance());
    }

    public static <T> DBSCAN<T> create(final double epsilon, final int minPts,
                                       final DistanceMeasure<? super T> distanceMeasure) {
        checkNotNull(distanceMeasure, "distanceMeasure");
        return create(epsilon, minPts, new NaiveNeighborSearch<>(distanceMeasure));
    }

    public static <T> DBSCAN<T> create(final double epsilon, final int minPts, final NeighborSearch<T> neighborSearch) {
        checkArgument(epsilon >= 0.0d, "epsilon must be positive");
        checkArgument(minPts >= 0, "minPts must be positive");
        checkNotNull(neighborSearch, "neighborSearch");
        return new DBSCAN<>(epsilon, minPts, neighborSearch);
    }

    @Override
    public DBSCANResult<T> apply(final Collection<T> objects) {
        checkNotNull(objects);

        final List<DBSCANCluster<T>> clusters = Lists.newArrayList();
        final List<T> noise = Lists.newArrayList();
        final Map<T, PointStatus> objectStatusMap = Maps.newIdentityHashMap();

        for (T object : objects) {
            objectStatusMap.put(object, PointStatus.UNKNOWN);
        }

        for (Map.Entry<T, PointStatus> entry : objectStatusMap.entrySet()) {
            if (entry.getValue().equals(PointStatus.UNKNOWN)) {

                final Optional<DBSCANCluster<T>> clusterOptional =
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

    private Optional<DBSCANCluster<T>> tryCluster(
            final T origin, final Map<T, PointStatus> objectStatusMap) {
        final List<T> clusterObjects = new ArrayList<>();
        final Queue<T> seeds = Queues.newArrayDeque();

        assert objectStatusMap.get(origin).equals(PointStatus.UNKNOWN);
        objectStatusMap.put(origin, PointStatus.SEED);
        seeds.offer(origin);

        final Set<T> candidates = objectStatusMap.keySet();

        while (!seeds.isEmpty()) {
            final T seed = seeds.poll();
            assert seed != null;

            final Collection<T> currentNeighbors =
                    neighborSearch.filterNeighbors(candidates, seed, epsilon);
            if (currentNeighbors.size() >= minPts) {
                for (T neighbor : currentNeighbors) {
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

    /**
     * A NeighborSearch algorithm that scans the whole collection for neighbours using a given {@link
     * org.asoem.greyfish.utils.space.DistanceMeasure distance measure}.
     *
     * @param <O> The type of the objects to search
     */
    public static final class NaiveNeighborSearch<O> implements NeighborSearch<O> {

        private final DistanceMeasure<? super O> distanceMeasure;

        public NaiveNeighborSearch(final DistanceMeasure<? super O> distanceMeasure) {
            checkNotNull(distanceMeasure);
            this.distanceMeasure = distanceMeasure;
        }

        @Override
        public Collection<O> filterNeighbors(
                final Collection<O> collection, final O origin,
                final double range) {
            return SpatialData.filterNeighbors(collection, origin, range, distanceMeasure);
        }
    }
}
