package org.asoem.sico.core.share;

import org.asoem.sico.core.individual.Individual;
import org.asoem.sico.core.properties.AbstractGFProperty;

public class Consumer<T extends AbstractGFProperty> {
	private Individual individual;
	private T property;

	public Consumer(Individual individual, T property) {
		if(individual == null || property == null)
			throw new IllegalArgumentException();

		this.individual = individual;
		this.property = property;
	}

	public Individual getIndividual() {
		return individual;
	}

	public T getProperty() {
		return property;
	}
}
