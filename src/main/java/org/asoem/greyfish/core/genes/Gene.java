package org.asoem.greyfish.core.genes;

import com.google.common.base.Supplier;
import org.asoem.greyfish.core.individual.AgentComponent;

import javax.annotation.Nullable;


public interface Gene<T> extends AgentComponent, Supplier<T> {

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
    boolean isMutatedCopy(@Nullable Gene<?> gene);

    /**
     * Computes the normalizedDistance between {@code this} and {@code that} using an arbitrary metric.
     * @param thatGene the gene to compute the distance to
     * @return the distance
     */
    double distance(Gene<?> thatGene);

    /**
     * Set the new value for this {@code Gene}
     * @param value the new value this gene will supply
     */
    void setValue(Object value);

    /**
     * Get the recombination probability for this gene.
     * The values is uses as the probability that, if this gene is on the focal chromosome,
     * the gene on the non-focal chromosome. If the non-focal is taken, at the next gene, the focal chromosome will be the currently non-focal.
     */
    double getRecombinationProbability();
}
