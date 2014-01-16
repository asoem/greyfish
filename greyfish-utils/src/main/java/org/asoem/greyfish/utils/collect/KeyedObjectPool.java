package org.asoem.greyfish.utils.collect;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public interface KeyedObjectPool<K, V> {
    /**
     * Borrow an element from this pool for given {@code key}.
     *
     * @return an element
     */
    V borrow(K key, Callable<? extends V> valueLoader) throws ExecutionException;

    /**
     * Release an element for given {@code key}.
     *
     * @param object the object to release
     */
    void release(K key, V object);
}
