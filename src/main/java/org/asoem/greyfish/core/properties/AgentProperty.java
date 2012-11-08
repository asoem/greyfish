package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.agent.AgentComponent;

public interface AgentProperty<T> extends AgentComponent {
    T getValue();
}
