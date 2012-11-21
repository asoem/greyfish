package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.agent.Agent;

import java.util.Set;

public interface FiniteStateProperty<T, A extends Agent<A, ?, ?>> extends AgentProperty<A, T> {
    Set<T> getStates();
}
