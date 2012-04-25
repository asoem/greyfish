package org.asoem.greyfish.core.genes;

/**
 * User: christoph
 * Date: 25.04.12
 * Time: 15:30
 */
public class GeneSnapshot<T> {
    private final T value;
    private final double recombinationProbability;

    public GeneSnapshot(T value, double recombinationProbability) {
        this.value = value;
        this.recombinationProbability = recombinationProbability;
    }

    public T getValue() {
        return value;
    }

    public double getRecombinationProbability() {
        return recombinationProbability;
    }
}
