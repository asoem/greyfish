package org.asoem.greyfish.utils.evolution;

public interface Recombination<T> {
    RecombinationProduct<T> recombine(T first, T second);
}
