package org.asoem.greyfish.core.utils;

/**
 * User: christoph
 * Date: 29.04.11
 * Time: 15:17
 */
public class EvaluatedCandidate<T> {
    private T object;
    private double fitness;

    public EvaluatedCandidate(T object, double fitness) {
        this.object = object;
        this.fitness = fitness;
    }

    public double getFitness() {
        return fitness;
    }

    public T getObject() {
        return object;
    }
}
