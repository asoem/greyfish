package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.individual.ComponentVisitor;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import java.util.Collections;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ImmutableGene<T> extends AbstractAgentComponent implements Gene<T> {

	private final T representation;
    private final Class<T> clazz;
    private final GeneController<T> geneController;

    /**
     * Constructor
     * @param element the initial value this gene will return using {@code get()}
     * @param clazz the Class of the supplied value
     * @param geneController the function which describes how to mutate the gene
     */
    public ImmutableGene(T element, Class<T> clazz, GeneController<T> geneController) {
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
	public String toString() {
		return "Gene[" + getSupplierClass().getSimpleName() + "]=" + String.valueOf(representation);
	}

    @Override
    public Class<T> getSupplierClass() {
        return clazz;
    }

    @Override
    public GeneController<T> getGeneController() {
        return geneController;
    }

    public static <E> ImmutableGene<E> newMutatedCopy(Gene<E> gene) {
        return new ImmutableGene<E>(
                gene.getGeneController().mutate(gene.get()),
                gene.getSupplierClass(),
                gene.getGeneController());
    }

    public static <E> ImmutableGene<E> newInitializedCopy(Gene<E> gene) {
        return new ImmutableGene<E>(
                gene.getGeneController().createInitialValue(),
                gene.getSupplierClass(),
                gene.getGeneController());
    }

    @Override
    public boolean isMutatedCopy(Gene<?> gene) {
        return this.getGeneController().equals(gene.getGeneController());
    }

    @Override
    public final double distance(Gene<?> thatGene) {
        checkArgument(this.getSupplierClass().equals(thatGene.getSupplierClass()));
        return getGeneController().normalizedDistance(this.get(), getSupplierClass().cast(thatGene.get()));
    }

    @Override
    public void set(T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void accept(ComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new ImmutableGene<T>(this, cloner);
    }

    public static <T> Gene<T> copyOf(Gene<T> gene) {
        return new ImmutableGene<T>(gene.get(), gene.getSupplierClass(), gene.getGeneController());
    }

    @Override
    public Iterable<AgentComponent> children() {
        return Collections.emptyList();
    }
}
