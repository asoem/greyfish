package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.agent.Agent;

public interface AgentContext<A extends Agent<A, ?>> {
    A agent();
}
