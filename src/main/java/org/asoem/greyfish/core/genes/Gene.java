package org.asoem.greyfish.core.genes;

import com.google.common.base.Supplier;


public interface Gene<T> extends Supplier<T> {
    public Class<T> getSupplierClass();
    public MutationOperator<T> getMutationFunction();
    boolean isMutatedCopyOf(Gene<?> gene);

    /**
     * Computes the normalizedDistance between {@code this} and {@code that} using an arbitrary metric.
     * @param thatGene
     * @return
     */
    double distance(Gene<?> thatGene);
}
