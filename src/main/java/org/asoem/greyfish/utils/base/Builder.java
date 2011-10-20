package org.asoem.greyfish.utils.base;

/**
 * User: christoph
 * Date: 20.01.11
 * Time: 16:12
 */
public interface Builder<T> {
    /**
     * Create a new instance of {@code E} based on this  builder of type {@code T}
     * @return a new instance of {@code E}
     * @throws IllegalStateException if this Builder is not in a state to produce a valid instance of {@code T}
     */
    T build() throws IllegalStateException;
}
