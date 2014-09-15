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
