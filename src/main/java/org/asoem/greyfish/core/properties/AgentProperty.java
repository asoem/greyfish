package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentComponent;

public interface AgentProperty<T, A extends Agent<?,A,?>> extends AgentComponent<A> {
    T getValue();
}
