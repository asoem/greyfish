package org.asoem.greyfish.core.genes;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultGene<T> implements Gene<T> {

	private final T representation;
    private final Class<T> clazz;
    private final MutationOperator<T> mutationFunction;

    /**
     * Constructor
     * @param element the initial value this gene will return using {@code get()}
     * @param clazz the Class of the supplied value
     * @param mutationFunction
     */
	public DefaultGene(T element, Class<T> clazz, MutationOperator<T> mutationFunction) {
        this.mutationFunction = mutationFunction;
        this.representation = checkNotNull(element);
        this.clazz = checkNotNull(clazz);
	}

    public DefaultGene(Gene<T> gene) {
        this.mutationFunction = gene.getMutationFunction();
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

    public static <T> Gene<T> newMutatedCopy(Gene<T> gene) {
        return new DefaultGene<T>(gene.getMutationFunction().mutate(gene.get()), gene.getSupplierClass(), gene.getMutationFunction());
    }

    @Override
    public boolean isMutatedCopyOf(Gene<?> gene) {
        return this.getMutationFunction().equals(gene.getMutationFunction());
    }

    @Override
    public final double distance(Gene<?> thatGene) {
        checkArgument(this.isMutatedCopyOf(thatGene));
        return mutationFunction.normalizedDistance(this.get(), getSupplierClass().cast(thatGene.get()));
    }
}
