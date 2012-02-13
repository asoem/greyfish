package org.asoem.greyfish.core.utils;

import com.google.common.primitives.Doubles;
import org.apache.commons.math.genetics.Fitness;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 29.04.11
 * Time: 15:17
 */
public class EvaluatedCandidate<T> implements Fitness, Comparable<EvaluatedCandidate<T>> {
    private final T object;
    private final double fitness;

    public EvaluatedCandidate(T object, double fitness) {
        this.object = checkNotNull(object);
        this.fitness = fitness;
    }

    public T getObject() {
        return object;
    }

    @Override
    public double fitness() {
        return fitness;
    }

    @Override
    public int compareTo(EvaluatedCandidate<T> o) {
        return Doubles.compare(this.fitness, o.fitness);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EvaluatedCandidate that = (EvaluatedCandidate) o;

        return Double.compare(that.fitness, fitness) == 0 && object.equals(that.object);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = object.hashCode();
        temp = fitness != +0.0d ? Double.doubleToLongBits(fitness) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
