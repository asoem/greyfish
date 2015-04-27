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

import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Set;

public final class DBSCANResult<O> implements ClusterResult<DBSCANCluster<O>> {

    private final Set<DBSCANCluster<O>> clusters;
    private final double eps;
    private final int minPts;
    private final Set<O> noise;

    private DBSCANResult(final Collection<? extends DBSCANCluster<O>> clusters,
                         final Collection<? extends O> noise, final double eps, final int minPts) {
        this.noise = ImmutableSet.copyOf(noise);
        this.clusters = ImmutableSet.copyOf(clusters);
        this.eps = eps;
        this.minPts = minPts;
    }


    static <O> DBSCANResult<O> create(
            final Collection<? extends DBSCANCluster<O>> clusters, final Collection<? extends O> noise,
            final double eps, final int minPts) {
        return new DBSCANResult<>(clusters, noise, eps, minPts);
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

        final DBSCANResult that = (DBSCANResult) o;

        if (Double.compare(that.eps, eps) != 0) {
            return false;
        }
        if (minPts != that.minPts) {
            return false;
        }
        if (!clusters.equals(that.clusters)) {
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
        return result;
    }

    public double getEps() {
        return eps;
    }

    public int getMinPts() {
        return minPts;
    }

    public Set<DBSCANCluster<O>> cluster() {
        return clusters;
    }

    public Set<O> noise() {
        return noise;
    }
}
