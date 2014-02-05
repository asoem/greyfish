package org.asoem.greyfish.utils.evolution;

public interface Mutation<T> {
    T mutate(T input);
}
