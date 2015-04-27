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

package org.asoem.greyfish.impl.environment;

import org.asoem.greyfish.impl.agent.BasicAgent;

import java.util.concurrent.Executor;

/**
 * The getSimulation environment for agents of type {@link org.asoem.greyfish.impl.agent.BasicAgent}.
 */
public interface BasicEnvironment extends SynchronizedAgentsEnvironment<BasicAgent> {
    /**
     * Add the removal of given {@code agent} to this simulations modification queue.
     *
     * @param agent the {@code Agent} to be removed from this {@code Simulation}
     */
    void enqueueRemoval(BasicAgent agent);

    /**
     * Add the removal of given {@code agent} to this simulations modification queue.
     *
     * @param agent the {@code Agent} to be removed from this {@code Simulation}
     */
    void enqueueRemoval(BasicAgent agent, Runnable listener, Executor executor);

    /**
     * Add the addition of given {@code agent} to this simulations modification queue.
     *
     * @param agent the agent to add
     */
    void enqueueAddition(BasicAgent agent);
}
