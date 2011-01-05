package org.asoem.sico.core.properties;

public interface ContinuosProperty<T extends Comparable<T>> extends GFProperty {
	public void setAmount(T amount);
	public T getAmount();
}
