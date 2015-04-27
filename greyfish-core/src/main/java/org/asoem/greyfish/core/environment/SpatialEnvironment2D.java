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

public interface SpatialEnvironment2D<A extends SpatialAgent<A, ?, ?, ?>, Z extends Space2D<A, ?>> extends DiscreteTimeEnvironment<A> {

    /**
     * Find all neighbours of {@code agent} within the given {@code distance}
     *
     * @param agent    The focal {@code agent}
     * @param distance The maximum allowed distance of an agent to count as a neighbour
     * @return all neighbours of {@code agent} within the given distance
     */
    Iterable<A> findNeighbours(A agent, double distance);

    /**
     * Get the space used in this getSimulation
     *
     * @return the space used in this getSimulation
     */
    Z getSpace();

    /**
     * Measure the distance from given agent to the first obstacle in the given direction (in degrees [0, 360))
     *
     * @param agent   the agent which serves as the origin of the distance measure
     * @param degrees the angle in which direction the distance will be measured
     * @return the distance from agent to the next object in this simulations space
     */
    double distance(A agent, double degrees);

    /**
     * Remove agent from this {@code Simulation}
     *
     * @param agent the {@code Agent} to be removed from this {@code Simulation}
     */
    void removeAgent(A agent);
}
