package org.asoem.greyfish.core.genes;

import com.google.common.base.Supplier;
import org.asoem.greyfish.core.individual.GFComponent;


public interface Gene<T> extends GFComponent, Supplier<T> {

    /**
     * @return the class of the value this gene is supplying
     */
    public Class<T> getSupplierClass();

    /**
     *
     * @return the controller for this gene
     */
    public GeneController<T> getGeneController();

    /**
     *
     * @param gene the gene to test for
     * @return {@code true} if {@code gene} is a mutated copy of this gene, {@code false} otherwise
     */
    boolean isMutatedCopyOf(Gene<?> gene);

    /**
     * Computes the normalizedDistance between {@code this} and {@code that} using an arbitrary metric.
     * @param thatGene the gene to compute the distance to
     * @return the distance
     */
    double distance(Gene<?> thatGene);
}
