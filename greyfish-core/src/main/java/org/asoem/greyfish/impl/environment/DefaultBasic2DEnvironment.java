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

import com.google.common.collect.ImmutableSet;
import org.asoem.greyfish.core.agent.DefaultActiveContext;
import org.asoem.greyfish.core.environment.Generic2DEnvironment;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.space.BasicTiled2DSpace;
import org.asoem.greyfish.utils.space.Point2D;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public final class DefaultBasic2DEnvironment
        extends Generic2DEnvironment<Basic2DAgent, Basic2DEnvironment, BasicTiled2DSpace, Point2D>
        implements Basic2DEnvironment {
    private final AtomicInteger agentIdSequence = new AtomicInteger();

    private DefaultBasic2DEnvironment(final Builder builder) {
        super(builder);
    }

    @Override
    protected void activateAgent(final Basic2DAgent agent) {
        agent.activate(DefaultActiveContext.create(self(), agentIdSequence.incrementAndGet(), getTime()));
    }

    @Override
    protected Basic2DEnvironment self() {
        return this;
    }

    public static Builder builder(final BasicTiled2DSpace space, final Basic2DAgent prototype) {
        return new Builder(space, ImmutableSet.of(prototype));
    }

    public static Builder builder(final BasicTiled2DSpace space, final Set<Basic2DAgent> prototypes) {
        return new Builder(space, prototypes);
    }

    @Override
    public void enqueueAddition(final Basic2DAgent agent, final Point2D point2D) {
        enqueueAgentCreation(agent, point2D);
    }

    public static final class Builder
            extends Generic2DEnvironmentBuilder<Builder, DefaultBasic2DEnvironment, Basic2DEnvironment, Basic2DAgent, BasicTiled2DSpace, Point2D> {

        public Builder(final BasicTiled2DSpace space, final Set<Basic2DAgent> prototypes) {
            super(space);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected DefaultBasic2DEnvironment checkedBuild() {
            return new DefaultBasic2DEnvironment(this);
        }
    }
}
