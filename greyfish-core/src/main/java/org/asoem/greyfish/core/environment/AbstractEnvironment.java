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
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.impl.environment.SynchronizedAgentsEnvironment;

public abstract class AbstractEnvironment<A extends Agent<?>> implements SynchronizedAgentsEnvironment<A> {


    @Override
    public final String toString() {
        return "Simulation['" + getName() + "']";
    }

    @Override
    public final Iterable<A> filterAgents(final Predicate<? super A> predicate) {
        return Iterables.filter(getActiveAgents(), predicate);
    }

    protected int standardCountAgents() {
        return Iterables.size(getActiveAgents());
    }

}
