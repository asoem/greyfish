package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;

public interface ContinuousProperty<T extends Comparable<T>> extends GFProperty, Supplier<T> {
	public void set(T amount);
}
