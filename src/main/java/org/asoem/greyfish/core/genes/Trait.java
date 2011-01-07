package org.asoem.greyfish.core.genes;

public interface Trait<T> {
	public T getValue();
	public void setValue(T value);
	
	public Gene<?>[] getGenes();
	public Gene<?> getGene(int index);
	public <R extends Gene<?>> R getGene(Class<R> clazz, int index);
	
	public String getName();
}
