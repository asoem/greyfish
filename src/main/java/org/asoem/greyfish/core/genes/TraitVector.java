package org.asoem.greyfish.core.genes;

import com.google.common.base.Supplier;

/**
 * User: christoph
 * Date: 25.04.12
 * Time: 15:30
 */
public class TraitVector<T> implements Supplier<T> {
    private final T value;
    private final double recombinationProbability;

    public TraitVector(T value, double recombinationProbability) {
        this.value = value;
        this.recombinationProbability = recombinationProbability;
    }

    @Override
    public T get() {
        return value;
    }

    public double getRecombinationProbability() {
        return recombinationProbability;
    }
}
