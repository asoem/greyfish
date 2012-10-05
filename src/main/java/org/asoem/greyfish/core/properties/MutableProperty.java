package org.asoem.greyfish.core.properties;

public interface MutableProperty<T extends Comparable<T>> extends AgentProperty<T> {
	void set(T amount);
}
