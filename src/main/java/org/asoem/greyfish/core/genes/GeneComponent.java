package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.utils.base.Tuple2;

import javax.annotation.Nullable;


public interface GeneComponent<T> extends AgentComponent, GeneLike<T> {

    /**
     * @return the class of the value this gene is supplying
     */
    Class<T> getSupplierClass();

    /**
     * @return the controller for this gene
     */
    GeneController<T> getGeneController();

    /**
     * @param gene the gene to builderTest for
     * @return {@code true} if {@code gene} is a mutated copy of this gene, {@code false} otherwise
     */
    boolean isMutatedCopy(@Nullable GeneComponent<?> gene);

    /**
     * Computes the normalizedDistance between {@code this} and {@code that} using an arbitrary metric.
     *
     * @param thatGene the gene to compute the distance to
     * @return the distance
     */
    double distance(GeneComponent<?> thatGene);

    /**
     * Set the new value for this {@code GeneComponent}
     *
     * @param value the new value this gene will supply
     */
    void setAllele(Object value);

    /**
     * Get the recombination probability for this gene.
     * The values is uses as the probability that, if this gene is on the focal geneComponentList,
     * the gene on the non-focal geneComponentList. If the non-focal is taken, at the next gene, the focal geneComponentList will be the currently non-focal.
     */
    double getRecombinationProbability();

    T mutatedValue();

    Tuple2<T, T> recombinedValue(T other);
}
