package org.asoem.greyfish.impl.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.simulation.Simulation;

public final class AgentRemovedEvent implements SimulationEvent {
    private final Agent<?, ?> agent;
    private Simulation<?> simulation;

    public AgentRemovedEvent(final Agent<?, ?> agent, final Simulation<?> simulation) {
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
