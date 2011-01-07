package org.asoem.greyfish.core.genes;

import com.google.common.base.Strings;


public abstract class AbstractGene<T> implements Gene<T> {

	protected  T representation;
	private String name;
	
	public AbstractGene(T element) {
		this.representation = element;
	}

	@Override
	public T getRepresentation() {
		return representation;
	}

	@Override
	public void setRepresentation(Object value) {
		this.representation = (T) value;
	}

	@Override
	public String toString() {
		return ((Strings.isNullOrEmpty(name)) ? "" : name + "=") + representation.toString();
	}

	@Override
	public String getName() {
		return name;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Gene<T> clone() {
		try {
			return (Gene<T>) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError();
		}
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
}
