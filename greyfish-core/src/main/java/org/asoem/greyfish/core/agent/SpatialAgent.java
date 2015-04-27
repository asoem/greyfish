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

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AgentContext;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Moving;
import org.asoem.greyfish.utils.space.Object2D;
import org.asoem.greyfish.utils.space.Projectable;

/**
 * An agent for spatial simulations.
 *
 * @param <A> the concrete type of this agent
 * @param <P> the type of the projection
 */
public interface SpatialAgent<
        A extends SpatialAgent<A, C, P, ?>,
        C extends Context<?, A>,
        P extends Object2D,
        AC extends AgentContext<A>>
        extends Agent<C>, Moving<Motion2D>, Projectable<P> {

    /**
     * Measure the distance from this agent to the first obstacle in the given direction (in {@code degrees} [0, 360))
     *
     * @param degrees the angle in which to measure the distance
     * @return the distance to the first obstacle
     * @see org.asoem.greyfish.core.environment.SpatialEnvironment2D#distance(SpatialAgent, double)
     */
    double distance(double degrees);

    /**
     * Find all agent which are located around this agent inside given {@code radius}.
     *
     * @param radius the radius of the search
     * @return all agents with distance smaller or equal to {@code radius} to this agent
     */
    Iterable<A> findNeighbours(double radius);

    Iterable<ACLMessage<A>> getMessages(MessageTemplate template);

}
