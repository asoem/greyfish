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

import com.google.common.collect.ForwardingCollection;
import com.google.common.collect.ImmutableList;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DBSCANCluster<O> extends ForwardingCollection<O>
        implements Cluster<O> {

    private final ImmutableList<O> objects;

    private DBSCANCluster(final ImmutableList<O> objects) {
        this.objects = objects;
    }

    @Override
    protected Collection<O> delegate() {
        return objects;
    }

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

        final DBSCANCluster that = (DBSCANCluster) o;

        if (!objects.equals(that.objects)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + objects.hashCode();
        return result;
    }

    public static <O> DBSCANCluster<O> create(final Collection<? extends O> objects) {
        checkNotNull(objects);
        return new DBSCANCluster<>(ImmutableList.copyOf(objects));
    }

}
