package org.asoem.greyfish.impl.agent;

import org.asoem.greyfish.core.actions.AgentContext;

public interface BasicAgentContext extends AgentContext<BasicAgent> {

    void addAgent(BasicAgent agent);

    void removeAgent(BasicAgent agent);
}
