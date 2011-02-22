package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.CloneMap;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractGene<T> extends AbstractDeepCloneable implements Gene<T> {

	private final T representation;
    private final Class<T> clazz;
	private final String name = "";
    private final MutationOperator<T> mutationFunction;

    /**
     * Constructor
     * @param element the initial value this gene will return using {@code get()}
     * @param clazz the Class of the supplied value
     * @param mutationFunction
     */
	public AbstractGene(T element, Class<T> clazz, MutationOperator<T> mutationFunction) {
        this.mutationFunction = mutationFunction;
        this.representation = checkNotNull(element);
        this.clazz = checkNotNull(clazz);
	}

    /**
     * DeepClone Constructor
     * @param gene the gene to clone
     * @param map a Map of originals to their clones
     */
    protected AbstractGene(AbstractGene<T> gene, CloneMap map) {
        super(gene, map);
        this.mutationFunction = gene.mutationFunction;
        this.clazz = gene.clazz;
        this.representation = gene.representation;
    }

    /**
     * Copy Constructor
     * @param gene the original to get copied
     * @param mutationFunction
     */
    protected AbstractGene(Gene<T> gene, MutationOperator<T> mutationFunction) {
        this.mutationFunction = mutationFunction;
        this.representation = gene.get();
        this.clazz = gene.getSupplierClass();
    }

    @Override
	public T get() {
		return representation;
	}

	@Override
	public String toString() {
		return "Gene[" + getSupplierClass().getSimpleName() + "]=" + String.valueOf(representation);
	}

    @Override
    public Class<T> getSupplierClass() {
        return clazz;
    }

    public MutationOperator<T> getMutationFunction() {
        return mutationFunction;
    }

    @Override
    public boolean isMutatedVersionOf(Gene<?> gene) {
        return this.getSupplierClass().equals(gene.getSupplierClass())
                && this.getMutationFunction().equals(gene.getMutationFunction());
    }

    @Override
    public final double distance(Gene<?> thatGene) {
        checkArgument(this.isMutatedVersionOf(thatGene));
        return mutationFunction.normalizedDistance(this.get(), getSupplierClass().cast(thatGene.get()));
    }
}
