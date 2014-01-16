package org.asoem.greyfish.utils.collect;

import com.google.common.base.Supplier;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.UncheckedExecutionException;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

public class SynchronizedKeyedObjectPool<K, V> implements KeyedObjectPool<K, V> {
    private final ListMultimap<K, V> multimap = Multimaps.synchronizedListMultimap(Multimaps.newListMultimap(Maps.<K, Collection<V>>newHashMap(), new Supplier<List<V>>() {
        @Override
        public List<V> get() {
            return Lists.newArrayList();
        }
    }));

    private SynchronizedKeyedObjectPool() {
    }

    public static <K, V> SynchronizedKeyedObjectPool<K, V> create() {
        return new SynchronizedKeyedObjectPool<>();
    }

    public static <K, V> LoadingSynchronizedKeyedObjectPool<K, V> create(final LoadingKeyedObjectPool.PoolLoader<K, V> valueLoader) {
        return new LoadingSynchronizedKeyedObjectPool<>(valueLoader);
    }

    @Override
    public V borrow(final K key, final Callable<? extends V> valueLoader) throws ExecutionException {
        checkNotNull(key);
        checkNotNull(valueLoader);

        final List<V> collection = multimap.get(key);
        synchronized (multimap) {
            if (collection.isEmpty()) {
                try {
                    return valueLoader.call();
                } catch (Exception e) {
                    throw new ExecutionException(e);
                } catch (Error e) {
                    throw new ExecutionError(e);
                } catch (Throwable e) {
                    throw new UncheckedExecutionException(e);
                }
            } else {
                return collection.remove(collection.size());
            }
        }
    }

    @Override
    public void release(final K key, final V object) {
        checkNotNull(key);
        checkNotNull(object);

        final List<V> collection = multimap.get(key);
        synchronized (multimap) {
            collection.add(object);
        }
    }

    private static class LoadingSynchronizedKeyedObjectPool<K, V> extends SynchronizedKeyedObjectPool<K, V> implements LoadingKeyedObjectPool<K, V> {
        private final PoolLoader<K, V> valueLoader;

        public LoadingSynchronizedKeyedObjectPool(final PoolLoader<K, V> valueLoader) {
            this.valueLoader = valueLoader;
        }

        @Override
        public V borrow(final K key) throws ExecutionException {
            return borrow(key, new Callable<V>() {
                @Override
                public V call() throws Exception {
                    return valueLoader.load(key);
                }
            });
        }
    }
}
