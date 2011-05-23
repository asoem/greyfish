package org.asoem.greyfish.core.genes;

/**
 * User: christoph
 * Date: 22.02.11
 * Time: 14:39
 */
public interface MutationOperator<T> {

    /**
     *
     * @param original The value to mutate
     * @return a mutated version of {@code original}
     */
    T mutate(T original);

    /**
     * @param orig The value to compare to
     * @param copy The value to compare
     * @return The normalized normalizedDistance (in range [0,1]) between the two supplied values {@code orig} and {@code copy}
     */
    double normalizedDistance(T orig, T copy);

    /**
     * @param orig The value to compare to
     * @param copy The value to compare
     * @return The normalized weighted normalizedDistance (in range [0,1]) between {@code orig} and {@code copy}
     */
    double normalizedWeightedDistance(T orig, T copy);

    /**
     * This function is intended to generate a random value to initialize a {@code Gene} when an agent is generated from a prototype.
     * @return a random value for this gene
     */
    T randomize();
}
