package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentComponent;
import org.asoem.greyfish.core.traits.Trait;

public interface AgentProperty<A extends Agent<A, ?>, T> extends AgentComponent<A>, Trait<T> {
}
