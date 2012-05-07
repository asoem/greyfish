package org.asoem.greyfish.core.properties;

public interface MutableProperty<T extends Comparable<T>> extends GFProperty {
	void set(T amount);
    T getValue();
}
