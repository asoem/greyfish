package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.agent.Agent;

public interface MutableProperty<T extends Comparable<T>, A extends Agent<A, ?, ?>> extends AgentProperty<A, T> {
	void set(T amount);
}
