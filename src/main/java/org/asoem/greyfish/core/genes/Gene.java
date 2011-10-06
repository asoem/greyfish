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
     *
     * @return the controller for this gene
     */
    GeneController<T> getGeneController();

    /**
     *
     *
     * @param gene the gene to test for
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
     * Set this genes current value to {@code value}
     * @param value the new value this gene will supply
     */
    void set(T value);
}
