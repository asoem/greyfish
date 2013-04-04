package org.asoem.greyfish.core.genes;

/**
 * User: christoph
 * Date: 25.04.12
 * Time: 15:30
 */
public class Gene<T> implements GeneLike<T> {
    private final T value;
    private final double recombinationProbability;

    public Gene(T value, double recombinationProbability) {
        this.value = value;
        this.recombinationProbability = recombinationProbability;
    }

    @Override
    public T getValue() {
        return value;
    }

    public double getRecombinationProbability() {
        return recombinationProbability;
    }
}
