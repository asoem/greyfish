package org.asoem.greyfish.impl.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.SimulationContext;
import org.asoem.greyfish.core.simulation.Simulation;

/**
 * This event is published when an {@link org.asoem.greyfish.core.agent.Agent agent} is added to a getSimulation.
 */
public final class AgentAddedEvent implements SimulationEvent {
    private final Agent<?, ? extends SimulationContext<?>> agent;
    private Simulation<?> simulation;

    public AgentAddedEvent(final Agent<?, ? extends SimulationContext<?>> agent, final Simulation<?> simulation) {
        this.agent = agent;
        this.simulation = simulation;
    }

    public Agent<?, ? extends SimulationContext<?>> getAgent() {
        return agent;
    }

    @Override
    public Simulation<?> getSimulation() {
        return simulation;
    }
}
