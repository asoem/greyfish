package org.asoem.greyfish.impl.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.simulation.Environment;

/**
 * This event is published when an {@link org.asoem.greyfish.core.agent.Agent agent} is added to a getSimulation.
 */
public final class AgentAddedEvent implements SimulationEvent {
    private final Agent<?> agent;
    private Environment<?> environment;

    public AgentAddedEvent(final Agent<?> agent, final Environment<?> environment) {
        this.agent = agent;
        this.environment = environment;
    }

    public Agent<?> getAgent() {
        return agent;
    }

    @Override
    public Environment<?> getEnvironment() {
        return environment;
    }
}
