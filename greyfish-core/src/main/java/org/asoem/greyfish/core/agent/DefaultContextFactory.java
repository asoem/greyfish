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
 * Default implementation of {@code SimulationContextFactory}.
 */
public final class DefaultContextFactory<S extends DiscreteTimeEnvironment<A>, A extends Agent<?>>
        implements ContextFactory<S, A> {

    private DefaultContextFactory() {
    }

    public static <S extends DiscreteTimeEnvironment<A>, A extends Agent<?>> DefaultContextFactory<S, A> create() {
        return new DefaultContextFactory<>();
    }

    @Override
    public BasicContext<S, A> createActiveContext(final S simulation, final int agentId, final long simulationStep) {
        return DefaultActiveContext.create(simulation, agentId, simulationStep);
    }

}
