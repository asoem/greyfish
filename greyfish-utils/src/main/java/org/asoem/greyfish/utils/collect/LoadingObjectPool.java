package org.asoem.greyfish.utils.collect;

import java.util.concurrent.ExecutionException;

/**
 * An object pool
 *
 * @param <T> the type of the objects
 */
public interface LoadingObjectPool<T> extends ObjectPool<T> {

    /**
     * Borrow an element from this pool
     *
     * @return an element
     */
    T borrow() throws ExecutionException;
}
