package org.asoem.greyfish.core.genes;

import com.google.common.base.Preconditions;
import com.google.common.collect.ObjectArrays;


public abstract class AbstractTrait<T> implements Trait<T> {

	private final String name;

	private static final Gene<?>[] NULL_GENES = new Gene[0];
	protected Gene<?>[] genes = NULL_GENES;

	public AbstractTrait() {
		this("AbstractTrait");
	}

	public AbstractTrait(String string) {
		name = string;
	}

	public AbstractTrait(String string, Gene<?> ... genes) {
		name = string;
		this.genes = new Gene<?>[genes.length];
		System.arraycopy(genes, 0, this.genes, 0, genes.length);
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Gene<?>[] getGenes() {
		return genes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R extends Gene<?>> R getGene(Class<R> clazz, int index) {
		Preconditions.checkPositionIndex(index, genes.length);
		Preconditions.checkArgument(clazz.isInstance(genes[index]));
		return (R) genes[index];
	}

	@Override
	public Gene<?> getGene(int index) {
		Preconditions.checkPositionIndex(index, genes.length);
		return genes[index];
	}

	@Override
	public void setValue(T value) {}

    public <R extends Gene<?>> R registerGene(final R gene) {
		if (genes == NULL_GENES)
			genes = new Gene<?>[] {gene};
		else
			ObjectArrays.concat(genes, gene);
		return gene;
	}
}
