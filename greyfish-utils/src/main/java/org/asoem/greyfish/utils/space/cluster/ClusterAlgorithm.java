package org.asoem.greyfish.utils.space.cluster;

import java.util.Set;

/**
 * Interface for cluster algorithms.
 *
 * @param <O> The type of the objects to cluster
 * @param <R> The type of the clusters that the algorithm returns
 */
public interface ClusterAlgorithm<O, R extends ClusterResult<?>> {

    /**
     * Apply the algorithm to a set of objects.
     *
     * @param objects the objects to cluster
     * @return the cluster result
     */
    R apply(Set<? extends O> objects);
}
