package org.asoem.greyfish.core.share;

import org.asoem.greyfish.core.individual.AbstractAgent;
import org.asoem.greyfish.core.properties.AbstractGFProperty;

public class Consumer<T extends AbstractGFProperty> {
	private AbstractAgent abstractAgent;
	private T property;

	public Consumer(AbstractAgent abstractAgent, T property) {
		if(abstractAgent == null || property == null)
			throw new IllegalArgumentException();

		this.abstractAgent = abstractAgent;
		this.property = property;
	}

	public AbstractAgent getAbstractAgent() {
		return abstractAgent;
	}

	public T getProperty() {
		return property;
	}
}
