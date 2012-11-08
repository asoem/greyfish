package org.asoem.greyfish.core.properties;

import java.util.Set;

public interface FiniteStateProperty<T> extends AgentProperty<T> {
    Set<T> getStates();
}
