package org.asoem.greyfish.impl.environment;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.environment.Environment;

/**
 * This event is published when an {@link org.asoem.greyfish.core.agent.Agent agent} is added to a getSimulation.
 */
public final class AgentAddedEvent implements EnvironmentModificationEvent {
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
