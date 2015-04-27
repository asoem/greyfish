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

import com.google.common.base.Predicate;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.Agent;

/**
 * A {@code Simulation} is an environment for interacting agents.
 */
public interface Environment<A extends Agent<?>> {

    /**
     * Get all agents in this simulation which satisfy the given {@code predicate}.
     *
     * @param predicate the predicate to check each agent against
     * @return all agents which satisfy the given {@code predicate}
     */
    Iterable<A> filterAgents(Predicate<? super A> predicate);

    /**
     * Get all agents in this simulation.
     *
     * @return an unmodifiable view of all active {@code Agent}s
     */
    Iterable<A> getActiveAgents();

    /**
     * Count all active Agents.
     *
     * @return The number of active Agents
     */
    int countAgents();

    /**
     * @return the name of this simulation
     */
    String getName();

    /**
     * Deliver given {@code message} to its destination.
     *
     * @param message the message to deliver
     */
    void deliverMessage(ACLMessage<A> message);

    /**
     * Get a status info for this simulation. The content is implementation specific.
     *
     * @return a message providing information about this simulation
     */
    String getStatusInfo();
}
