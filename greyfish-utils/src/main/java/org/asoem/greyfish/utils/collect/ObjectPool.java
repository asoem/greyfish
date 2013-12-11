package org.asoem.greyfish.utils.collect;

/**
 * An object pool
 *
 * @param <T> the type of the objects
 */
public interface ObjectPool<T> {

    /**
     * Borrow an element from this pool
     *
     * @return an element
     */
    T borrow();

    /**
     * Release an element
     *
     * @param object
     */
    void release(final T object);
}
