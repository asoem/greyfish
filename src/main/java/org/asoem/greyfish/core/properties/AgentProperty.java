package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentComponent;

public interface AgentProperty<A extends Agent<A, ?>, T> extends AgentComponent<A> {
    T getValue();
}
