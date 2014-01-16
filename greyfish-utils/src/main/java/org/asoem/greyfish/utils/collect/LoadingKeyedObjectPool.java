package org.asoem.greyfish.utils.collect;

import java.util.concurrent.ExecutionException;

public interface LoadingKeyedObjectPool<K, V> extends KeyedObjectPool<K, V> {
    V borrow(K key) throws ExecutionException;

    public interface PoolLoader<K, V> {
        V load(K key) throws Exception;
    }
}
