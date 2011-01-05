package org.asoem.sico.core.properties;

import java.util.Map;

import org.asoem.sico.core.individual.Individual;
import org.asoem.sico.utils.AbstractDeepCloneable;

public class IndividualsCollectionProperty extends AbstractDiscreteProperty<Individual[]> implements ContinuosProperty<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2889079556885154595L;
	private static final Individual[] NULL_ARRAY = new Individual[0];

	public IndividualsCollectionProperty() {
		value = new Individual[0];
	}

	protected IndividualsCollectionProperty(
			IndividualsCollectionProperty individualsCollectionProperty,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(individualsCollectionProperty, mapDict);
		value = new Individual[0];
	}

	public void addIndividual(Individual individual) {
		Individual[] temp = new Individual[value.length + 1];
		System.arraycopy(value, 0, temp, 0, value.length);
		temp[value.length] = individual;
		value = temp;
	}

	public Individual[] getIndividuals() {
		return getValue();
	}

	public void setIndividuals(Individual[] individuals) {
		this.value = (individuals.length == 0) ? NULL_ARRAY : individuals;
	}

	public void clear() {
		this.value = NULL_ARRAY;
	}

	@Override
	public Integer getAmount() {
		return value.length;
	}
	
	@Override
	public void setAmount(Integer amount) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new IndividualsCollectionProperty(this, mapDict);
	}
}
