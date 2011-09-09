package org.asoem.greyfish.core.share;

import org.asoem.greyfish.core.individual.DefaultAgent;
import org.asoem.greyfish.core.properties.AbstractGFProperty;

public class Consumer<T extends AbstractGFProperty> {
	private DefaultAgent defaultAgent;
	private T property;

	public Consumer(DefaultAgent defaultAgent, T property) {
		if(defaultAgent == null || property == null)
			throw new IllegalArgumentException();

		this.defaultAgent = defaultAgent;
		this.property = property;
	}

	public DefaultAgent getDefaultAgent() {
		return defaultAgent;
	}

	public T getProperty() {
		return property;
	}
}
