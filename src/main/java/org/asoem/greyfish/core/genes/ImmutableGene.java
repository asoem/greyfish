package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import static com.google.common.base.Preconditions.checkNotNull;

public class ImmutableGene<T> extends AbstractGene<T> {

	private final T representation;
    private final Class<T> clazz;
    private final GeneController<T> geneController;

    /**
     * Constructor
     * @param name the name for this gene
     * @param element the initial value this gene will return using {@code get()}
     * @param clazz the Class of the supplied value
     * @param geneController the function which describes how to mutate the gene
     */
    public ImmutableGene(String name, T element, Class<T> clazz, GeneController<T> geneController) {
        super(name);
        this.geneController = checkNotNull(geneController);
        this.representation = checkNotNull(element);
        this.clazz = checkNotNull(clazz);
	}

    private ImmutableGene(ImmutableGene<T> gene, DeepCloner map) {
        super(gene, map);
        this.geneController = checkNotNull(gene.getGeneController());
        this.representation = checkNotNull(gene.get());
        this.clazz = checkNotNull(gene.getSupplierClass());
    }

    @Override
	public T get() {
		return representation;
	}

    @Override
    public Class<T> getSupplierClass() {
        return clazz;
    }

    @Override
    public GeneController<T> getGeneController() {
        return geneController;
    }

    @Override
    public void setValue(Object value) {
        throw new UnsupportedOperationException();
    }

    public static <E> ImmutableGene<E> newMutatedCopy(Gene<E> gene) {
        checkNotNull(gene);
        return new ImmutableGene<E>(
                gene.getName(),
                gene.getGeneController().mutate(gene.get()),
                gene.getSupplierClass(),
                gene.getGeneController());
    }

    public static <E> ImmutableGene<E> newInitializedCopy(Gene<E> gene) {
        checkNotNull(gene);
        return new ImmutableGene<E>(
                gene.getName(),
                gene.getGeneController().createInitialValue(),
                gene.getSupplierClass(),
                gene.getGeneController());
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new ImmutableGene<T>(this, cloner);
    }

    public static <T> Gene<T> copyOf(Gene<T> gene) {
        return new ImmutableGene<T>(gene.getName(), gene.get(), gene.getSupplierClass(), gene.getGeneController());
    }

    public static <T> ImmutableGene<T> of(String name, T element, Class<T> elementType, GeneController<T> geneController) {
        return new ImmutableGene<T>(name, element, elementType, geneController);
    }
}
