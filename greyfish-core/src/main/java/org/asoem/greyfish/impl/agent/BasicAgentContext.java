package org.asoem.greyfish.impl.agent;

import org.asoem.greyfish.core.actions.AgentContext;
import org.asoem.greyfish.core.agent.Agent;

public interface BasicAgentContext<A extends Agent<?>> extends AgentContext<A> {
    void addAgent(A agent);

    void removeAgent(A agent);
}
