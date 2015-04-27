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
