package org.asoem.greyfish.core.genes;

import com.google.common.base.Supplier;
import org.asoem.greyfish.utils.DeepCloneable;


public interface Gene<T> extends DeepCloneable, Supplier<T> {
    public Class<T> getSupplierClass();
    public MutationOperator<T> getMutationFunction();

    public Gene<T> mutatedCopy();

    boolean isMutatedVersionOf(Gene<?> gene);

    /**
     * Computes the normalizedDistance between {@code this} and {@code that} using an arbitrary metric.
     * @param thatGene
     * @return
     */
    double distance(Gene<?> thatGene);
}
