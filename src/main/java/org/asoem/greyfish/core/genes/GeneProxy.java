package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class GeneProxy<T> implements Gene<T> {

    private Gene<T> gene;
    private final Class<T> clazz;
    private int index = -1;

    /**
     * Copy Constructor
     * @param gene the gene to forward methods to
     */
    public GeneProxy(Gene<T> gene) {
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

    @SuppressWarnings("unchecked")
    public GeneProxy(GeneProxy<T> geneProxy, CloneMap map) {
        this.gene = (Gene<T>) map.clone(geneProxy.gene, Gene.class);
        this.clazz = geneProxy.getSupplierClass();
    }

    @Override
    public <T extends DeepCloneable> T deepClone(Class<T> clazz) {
        return clazz.cast(deepCloneHelper(CloneMap.newInstance()));
    }

    @Override
    public DeepCloneable deepCloneHelper(CloneMap map) {
        return new GeneProxy<T>(this, map);
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
    public Gene<T> mutatedCopy() {
        return new GeneProxy<T>(gene.mutatedCopy());
    }

    @Override
    public boolean isMutatedVersionOf(Gene<?> gene) {
        return this.gene.isMutatedVersionOf(gene);
    }

    public static <T> GeneProxy<T> newInstance(Gene<T> gene) {
        return new GeneProxy<T>(gene);
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

    /**
     *
     * @return The index of the delegate gene in it's managing genome
     */
    public int getIndex() {
        return index;
    }

    @Override
    public double distance(Gene<?> thatGene) {
        return gene.distance(thatGene);
    }
}
