package org.asoem.greyfish.impl.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.simulation.Environment;

public final class AgentRemovedEvent implements SimulationEvent {
    private final Agent<?> agent;
    private Environment<?> environment;

    public AgentRemovedEvent(final Agent<?> agent, final Environment<?> environment) {
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
