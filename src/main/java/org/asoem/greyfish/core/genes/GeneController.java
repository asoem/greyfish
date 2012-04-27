package org.asoem.greyfish.core.genes;

/**
 * User: christoph
 * Date: 22.02.11
 * Time: 14:39
 *
 * Classes implementing this Interface (strategy) define how the underlying gene (context)
 * should compute a mutated mutated version of itself and the distance to a mutated copy.
 */
public interface GeneController<T> {

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
     * This function is intended to generate a value to which a {@code GeneComponent}
     * will be initialize when (and probably ony when) an agent is generated from a prototype.
     * @return some value dependent on the implementation
     */
    T createInitialValue();
}
