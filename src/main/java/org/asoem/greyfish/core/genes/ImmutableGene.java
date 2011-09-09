package org.asoem.greyfish.core.genes;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ImmutableGene<T> implements Gene<T> {

	private final T representation;
    private final Class<T> clazz;
    private final GeneController<T> mutationFunction;

    /**
     * Constructor
     * @param element the initial value this gene will return using {@code get()}
     * @param clazz the Class of the supplied value
     * @param mutationFunction
     */
	public ImmutableGene(T element, Class<T> clazz, GeneController<T> mutationFunction) {
        this.mutationFunction = checkNotNull(mutationFunction);
        this.representation = checkNotNull(element);
        this.clazz = checkNotNull(clazz);
	}

    public ImmutableGene(Gene<T> gene) {
        this.mutationFunction = checkNotNull(gene.getGeneController());
        this.representation = checkNotNull(gene.get());
        this.clazz = checkNotNull(gene.getSupplierClass());
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

    public GeneController<T> getGeneController() {
        return mutationFunction;
    }

    public static <T> Gene<T> newMutatedCopy(Gene<T> gene) {
        return new ImmutableGene<T>(gene.getGeneController().mutate(gene.get()), gene.getSupplierClass(), gene.getGeneController());
    }

    public static <T> Gene<T> newInitializedCopy(Gene<T> gene) {
        return new ImmutableGene<T>(gene.getGeneController().initialize(), gene.getSupplierClass(), gene.getGeneController());
    }

    @Override
    public boolean isMutatedCopyOf(Gene<?> gene) {
        return this.getGeneController().equals(gene.getGeneController());
    }

    @Override
    public final double distance(Gene<?> thatGene) {
        checkArgument(this.getSupplierClass().equals(thatGene.getSupplierClass()));
        return mutationFunction.normalizedDistance(this.get(), getSupplierClass().cast(thatGene.get()));
    }

    public static <T> Gene<T> copyOf(Gene<T> next) {
        return new ImmutableGene<T>(next);
    }
}
