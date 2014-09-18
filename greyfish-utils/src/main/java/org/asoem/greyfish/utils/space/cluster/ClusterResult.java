package org.asoem.greyfish.utils.space.cluster;

import java.util.Set;

/**
 * The interface for results of cluster algorithms.
 *
 * @param <C> the type of cluster the algorithm returns.
 */
public interface ClusterResult<C extends Cluster<?>> {
    /**
     * Get the clusters.
     *
     * @return a set of clusters
     */
    Set<C> cluster();
}
