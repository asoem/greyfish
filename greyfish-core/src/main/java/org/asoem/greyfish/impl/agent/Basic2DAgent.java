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

package org.asoem.greyfish.impl.agent;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.BasicContext;
import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.impl.environment.Basic2DEnvironment;
import org.asoem.greyfish.utils.collect.FunctionalList;
import org.asoem.greyfish.utils.space.Point2D;

/**
 * A basic configuration of a spatial agent with an {@link Point2D} projection into space to get simulated in an {@link
 * org.asoem.greyfish.impl.environment.Basic2DEnvironment}.
 */
public interface Basic2DAgent extends SpatialAgent<Basic2DAgent, BasicContext<Basic2DEnvironment, Basic2DAgent>, Point2D, Basic2DAgentContext> {
    /**
     * Get all actions of this agent
     *
     * @return the actions of this agent
     */
    FunctionalList<AgentAction<? super Basic2DAgentContext>> getActions();

    /**
     * Get all traits of this agent
     *
     * @return the traits of this agent
     */
    FunctionalList<AgentTrait<? super Basic2DAgentContext, ?>> getTraits();

    /**
     * Get all properties of this agent
     *
     * @return the properties of this agent
     */
    FunctionalList<AgentProperty<? super Basic2DAgentContext, ?>> getProperties();
}
