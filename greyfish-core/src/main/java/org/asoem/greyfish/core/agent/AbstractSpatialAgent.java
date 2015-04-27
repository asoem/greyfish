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

package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentContext;
import org.asoem.greyfish.core.environment.SpatialEnvironment2D;
import org.asoem.greyfish.utils.space.Object2D;

/**
 * Base implementation of a spatial agent
 */
public abstract class AbstractSpatialAgent<A extends SpatialAgent<A, BasicContext<S, A>, P, ?>, S extends SpatialEnvironment2D<A, ?>, P extends Object2D, AC extends AgentContext<A>>
        extends AbstractAgent<A, BasicContext<S, A>, AC> implements SpatialAgent<A, BasicContext<S, A>, P, AC> {

    @Override
    public double distance(final double degrees) {
        return getContext().get().getEnvironment().distance(self(), degrees);
    }

    @Override
    public Iterable<A> findNeighbours(final double radius) {
        return getContext().get().getEnvironment().findNeighbours(self(), radius);
    }
}
