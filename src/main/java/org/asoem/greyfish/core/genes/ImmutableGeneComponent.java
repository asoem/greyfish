package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import static com.google.common.base.Preconditions.checkNotNull;

public class ImmutableGeneComponent<T> extends AbstractGeneComponent<T> {

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
    public ImmutableGeneComponent(String name, T element, Class<T> clazz, GeneController<T> geneController) {
        super(name);
        this.geneController = checkNotNull(geneController);
        this.representation = checkNotNull(element);
        this.clazz = checkNotNull(clazz);
	}

    private ImmutableGeneComponent(ImmutableGeneComponent<T> gene, DeepCloner map) {
        super(gene, map);
        this.geneController = checkNotNull(gene.getGeneController());
        this.representation = checkNotNull(gene.getValue());
        this.clazz = checkNotNull(gene.getSupplierClass());
    }

    @Override
	public T getValue() {
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

    public static <E> ImmutableGeneComponent<E> newMutatedCopy(GeneComponent<E> gene) {
        checkNotNull(gene);
        return new ImmutableGeneComponent<E>(
                gene.getName(),
                gene.getGeneController().mutate(gene.getValue()),
                gene.getSupplierClass(),
                gene.getGeneController());
    }

    public static <E> ImmutableGeneComponent<E> newInitializedCopy(GeneComponent<E> gene) {
        checkNotNull(gene);
        return new ImmutableGeneComponent<E>(
                gene.getName(),
                gene.getGeneController().createInitialValue(),
                gene.getSupplierClass(),
                gene.getGeneController());
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new ImmutableGeneComponent<T>(this, cloner);
    }

    public static <T> GeneComponent<T> copyOf(GeneComponent<T> gene) {
        return new ImmutableGeneComponent<T>(gene.getName(), gene.getValue(), gene.getSupplierClass(), gene.getGeneController());
    }

    public static <T> ImmutableGeneComponent<T> of(String name, T element, Class<T> elementType, GeneController<T> geneController) {
        return new ImmutableGeneComponent<T>(name, element, elementType, geneController);
    }
}
