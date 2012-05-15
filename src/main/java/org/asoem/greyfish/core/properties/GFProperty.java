package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.individual.AgentComponent;

public interface GFProperty<T> extends AgentComponent {
    T getValue();
}
