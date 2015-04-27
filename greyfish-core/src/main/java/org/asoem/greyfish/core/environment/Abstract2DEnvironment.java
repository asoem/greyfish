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

package org.asoem.greyfish.core.environment;

import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.core.space.Space2D;

/**
 * Basic class for implementing 2D simulations.
 */
public abstract class Abstract2DEnvironment<A extends SpatialAgent<A, ?, ?, ?>, Z extends Space2D<A, ?>>
        extends AbstractEnvironment<A> implements SpatialEnvironment2D<A, Z> {

    @Override
    public final Iterable<A> findNeighbours(final A agent, final double distance) {
        return getSpace().getVisibleNeighbours(agent, distance);
    }

    @Override
    public final Iterable<A> getActiveAgents() {
        return getSpace().getObjects();
    }

    @Override
    public final int countAgents() {
        return getSpace().countObjects();
    }

    /**
     * Remove agent from this {@code Simulation}
     *
     * @param agent the {@code Agent} to be removed from this {@code Simulation}
     */
    public abstract void removeAgent(A agent);
}
