package org.asoem.greyfish.core.genes;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class IndexedGene<T> implements Gene<T> {

    private Gene<T> gene;
    private final Class<T> clazz;
    private int index = -1;

    /**
     * Copy Constructor
     * @param gene the gene to forward methods to
     */
    public IndexedGene(Gene<T> gene) {
        this.gene = checkNotNull(gene);
        clazz = gene.getSupplierClass();
    }

    @SuppressWarnings("unchecked")
    public void setGene(Gene<?> gene) {
        checkArgument(clazz.equals(gene.getSupplierClass()),
                "type of the given gene's supplier class do not match this gene's supplier class: "
                        + clazz + " != " + getSupplierClass());
        this.gene = Gene.class.cast(gene);
    }


    @Override
    public T get() {
        return gene.get();
    }

    @Override
    public Class<T> getSupplierClass() {
        return clazz;
    }

    @Override
    public MutationOperator<T> getMutationFunction() {
        return gene.getMutationFunction();
    }

    @Override
    public boolean isMutatedVersionOf(Gene<?> gene) {
        return this.gene.isMutatedVersionOf(gene);
    }

    public static <T> IndexedGene<T> newInstance(Gene<T> delegate) {
        return new IndexedGene<T>(delegate);
    }

    public Gene<T> getGene() {
        return gene;
    }

    /**
     * Set the index of the delegate in the genome which manages it.
     * @param index The index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public double distance(Gene<?> thatGene) {
        return gene.distance(thatGene);
    }
}
