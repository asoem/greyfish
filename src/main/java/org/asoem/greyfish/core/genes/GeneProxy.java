package org.asoem.greyfish.core.genes;

import com.google.common.base.Function;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class GeneProxy<T> implements Gene<T> {

    private Gene<T> gene;

    /**
     * Copy Constructor
     * @param gene the gene to forward methods to
     */
    public GeneProxy(Gene<T> gene) {
        setGene(gene);
    }

    @SuppressWarnings("unchecked")
    public void setGene(Gene<?> gene) {
        checkNotNull(gene);
        checkClassesMatch(gene.getSupplierClass());
        this.gene = this.gene.getClass().cast(gene);
    }

    @SuppressWarnings("unchecked")
    public GeneProxy(GeneProxy<T> geneProxy, CloneMap map) {
        checkClassesMatch(geneProxy.getSupplierClass());
        this.gene = (Gene<T>) map.clone(geneProxy.gene, Gene.class);
    }

    private void checkClassesMatch(Class<?> clazz) {
        checkArgument(getSupplierClass().equals(clazz),
                "type of the given gene's supplier class do not match this gene's supplier class: "
                        + clazz + " != " + getSupplierClass());
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
        return gene.getSupplierClass();
    }

    @Override
    public Function<T, T> getMutationFunction() {
        return gene.getMutationFunction();
    }

    @Override
    public void initialize() {
        gene.initialize();
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


}
