package org.asoem.greyfish.impl.agent;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.BasicSimulationContext;
import org.asoem.greyfish.core.agent.Descendant;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.impl.simulation.BasicSimulation;
import org.asoem.greyfish.utils.collect.FunctionalList;

/**
 * A basic agent configuration to be simulated in a {@link org.asoem.greyfish.impl.simulation.BasicSimulation}.
 */
public interface BasicAgent extends Agent<BasicSimulationContext<BasicSimulation, BasicAgent>>, Descendant {

    /**
     * Get all actions of this agent
     *
     * @return the actions of this agent
     */
    FunctionalList<AgentAction<? super BasicAgentContext<BasicAgent>>> getActions();

    /**
     * Get all properties of this agent
     *
     * @return the properties of this agent
     */
    FunctionalList<AgentProperty<? super BasicAgentContext<BasicAgent>, ?>> getProperties();
}
