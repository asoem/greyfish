package org.asoem.greyfish.impl.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.simulation.Simulation;

/**
 * This event is published when an {@link org.asoem.greyfish.core.agent.Agent agent} is added to a getSimulation.
 */
public final class AgentAddedEvent implements SimulationEvent {
    private final Agent<?, ?> agent;
    private Simulation<?> simulation;

    public AgentAddedEvent(final Agent<?, ?> agent, final Simulation<?> simulation) {
        this.agent = agent;
        this.simulation = simulation;
    }

    public Agent<?, ?> getAgent() {
        return agent;
    }

    @Override
    public Simulation<?> getSimulation() {
        return simulation;
    }
}
