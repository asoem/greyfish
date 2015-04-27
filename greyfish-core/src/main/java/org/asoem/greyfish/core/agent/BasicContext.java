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

import org.asoem.greyfish.core.environment.DiscreteTimeEnvironment;

/**
 * A SimulationContext is the link between an {@link Agent} and a {@link org.asoem.greyfish.core.environment.DiscreteTimeEnvironment}.
 * If an agent got activated a newly created context will be set for this agent.
 */
public interface BasicContext<S extends DiscreteTimeEnvironment<A>, A extends Agent<?>> extends Context<S, A> {

    /**
     * The step at which this agent was inserted into the getSimulation.
     *
     * @return the activation step
     */
    long getActivationStep();

    /**
     * Get the age of this agent. Same as calling {@code getSimulation().getSteps() - getActivationStep()}
     *
     * @return the difference between the activation step and current step
     */
    long getAge();

    /**
     * Get the current getSimulation step. Delegates to {@link org.asoem.greyfish.core.environment.DiscreteTimeEnvironment#getTime()}
     *
     * @return the number of executed steps in the getSimulation
     */
    long getSimulationStep();

    /**
     * Get the current simulation time. <p>Same as calling {@code getSimulation().getTime()}</p>
     *
     * @return the current time of the simulation
     */
    long getTime();

    String simulationName();
}
