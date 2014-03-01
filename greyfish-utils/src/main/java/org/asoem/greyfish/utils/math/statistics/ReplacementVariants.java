package org.asoem.greyfish.utils.math.statistics;

public interface ReplacementVariants<E> {
    Sampling<E> withReplacement();

    Sampling<E> withoutReplacement();
}
