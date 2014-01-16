package org.asoem.greyfish.utils.collect;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public interface ObjectPool<T> {
    /**
     * Borrow an object from this pool. If the pool is empty a new object is created using the {@code valueLoader}.
     *
     * @param valueLoader the factory for new objects
     * @return an object borrowed from the pool, or a newly created one from {@code valueLoader}
     * @throws ExecutionException
     */
    T borrow(Callable<T> valueLoader) throws ExecutionException;

    /**
     * Release an element
     *
     * @param object the object to release
     */
    void release(T object);
}
