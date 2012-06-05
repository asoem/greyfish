package org.asoem.greyfish.core.genes;

/**
 * User: christoph
 * Date: 05.06.12
 * Time: 14:10
 */
public interface GeneLike<T> {
    /**
     * Get the current value of this gene
     *
     * @return the current value of this gene
     */
    T getAllele();
}
