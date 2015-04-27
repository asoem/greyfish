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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A getSimulation context which indicates an active state of an agent.
 *
 * @param <S> the type of the getSimulation
 * @param <A> the type of the agent
 */
public final class DefaultActiveContext<S extends DiscreteTimeEnvironment<A>, A extends Agent<?>> implements BasicContext<S, A> {

    private final S simulation;

    private final long activationStep;

    private final int agentId;

    private DefaultActiveContext(final S simulation, final int agentId, final long simulationStep) {
        this.simulation = checkNotNull(simulation);
        this.agentId = agentId;
        this.activationStep = simulationStep;
    }

    public static <S extends DiscreteTimeEnvironment<A>, A extends Agent<?>>
    DefaultActiveContext<S, A> create(final S simulation, final int agentId, final long simulationStep) {
        return new DefaultActiveContext<>(simulation, agentId, simulationStep);
    }

    @Override
    public long getActivationStep() {
        return activationStep;
    }

    @Override
    public S getEnvironment() {
        return simulation;
    }

    @Override
    public Iterable<A> getActiveAgents() {
        return simulation.getActiveAgents();
    }

    @Override
    public long getAge() {
        checkState(getSimulationStep() >= getActivationStep(),
                "Agent seems to be born in the future: activationStep={} > simulation.getTime()={}",
                getActivationStep(), simulation.getTime());

        return getEnvironment().getTime() - getActivationStep();
    }

    @Override
    public long getSimulationStep() {
        return getEnvironment().getTime();
    }

    @Override
    public long getTime() {
        return getEnvironment().getTime();
    }

    @Override
    public String simulationName() {
        return getEnvironment().getName();
    }

    private static final long serialVersionUID = 0;
}
