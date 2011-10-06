package org.asoem.greyfish.core.share;

import org.asoem.greyfish.core.individual.MutableAgent;
import org.asoem.greyfish.core.properties.AbstractGFProperty;

public class Consumer<T extends AbstractGFProperty> {
	private MutableAgent mutableAgent;
	private T property;

	public Consumer(MutableAgent mutableAgent, T property) {
		if(mutableAgent == null || property == null)
			throw new IllegalArgumentException();

		this.mutableAgent = mutableAgent;
		this.property = property;
	}

	public MutableAgent getMutableAgent() {
		return mutableAgent;
	}

	public T getProperty() {
		return property;
	}
}
