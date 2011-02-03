package org.asoem.greyfish.core.genes;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.asoem.greyfish.utils.DeepCloneable;


public interface Gene<T> extends DeepCloneable, Supplier<T> {
    public Class<T> getSupplierClass();
    public Function<T, T> getMutationFunction();

    public Gene<T> mutatedCopy();

    boolean isMutatedVersionOf(Gene<?> gene);
}
