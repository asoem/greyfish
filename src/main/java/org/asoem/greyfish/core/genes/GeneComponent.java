package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.individual.AgentComponent;

import javax.annotation.Nullable;


public interface GeneComponent<T> extends AgentComponent, GeneLike<T> {

    /**
     * @return the class of the value this gene is supplying
     */
    Class<T> getAlleleClass();

    /**
     * @param gene the gene to builderTest for
     * @return {@code true} if {@code gene} is a mutated copy of this gene, {@code false} otherwise
     */
    boolean isMutatedCopy(@Nullable GeneComponent<?> gene);

    /**
     * Set the new value for this {@code GeneComponent}
     *
     * @param allele
     */
    void setAllele(Object allele);

    /**
     * Get the recombination probability for this gene.
     * The values is uses as the probability that, if this gene is on the focal geneComponentList,
     * the gene on the non-focal geneComponentList. If the non-focal is taken, at the next gene, the focal geneComponentList will be the currently non-focal.
     */
    double getRecombinationProbability();

    T mutate(T allele);

    T segregate(T allele1, T allele2);

    T createInitialValue();
}
