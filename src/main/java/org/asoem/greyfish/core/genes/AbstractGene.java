package org.asoem.greyfish.core.genes;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.CloneMap;

public abstract class AbstractGene<T> extends AbstractDeepCloneable implements Gene<T> {

	T representation;
	private String name = "";
	
	public AbstractGene(T element) {
		this.representation = element;
	}

    AbstractGene(AbstractGene<T> doubleGene, CloneMap map) {
        super(doubleGene, map);
    }

    @Override
	public T getRepresentation() {
		return representation;
	}

	@Override
	public void setRepresentation(Object value, Class<T> clazz) {
		this.representation = clazz.cast(value);
	}

	@Override
	public String toString() {
		return "Gene[" + getName() + "]=" + String.valueOf(representation);
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = Preconditions.checkNotNull(name);
	}
}
