/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.utils.collect;

import com.google.common.base.Supplier;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.UncheckedExecutionException;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static com.google.common.base.Preconditions.checkNotNull;

public class SynchronizedKeyedObjectPool<K, V> implements KeyedObjectPool<K, V> {
    private final ListMultimap<K, V> multimap = Multimaps.synchronizedListMultimap(
            Multimaps.newListMultimap(
                    Maps.<K, Collection<V>>newHashMap(),
                    new Supplier<List<V>>() {
                        @Override
                        public List<V> get() {
                            return Lists.newArrayList();
                        }
                    }));
    private ExecutorService executorService;

    private SynchronizedKeyedObjectPool(final ExecutorService executorService) {
        this.executorService = executorService;
    }

    public static <K, V> SynchronizedKeyedObjectPool<K, V> create() {
        final ListeningExecutorService executorService = MoreExecutors.newDirectExecutorService();
        return create(executorService);
    }

    static <K, V> SynchronizedKeyedObjectPool<K, V> create(final ListeningExecutorService executorService) {
        return new SynchronizedKeyedObjectPool<>(executorService);
    }

    public static <K, V> LoadingSynchronizedKeyedObjectPool<K, V> create(
            final LoadingKeyedObjectPool.PoolLoader<K, V> valueLoader) {
        final ListeningExecutorService executorService = MoreExecutors.newDirectExecutorService();
        return create(valueLoader, executorService);
    }

    static <K, V> LoadingSynchronizedKeyedObjectPool<K, V> create(
            final LoadingKeyedObjectPool.PoolLoader<K, V> valueLoader,
            final ListeningExecutorService executorService) {
        return new LoadingSynchronizedKeyedObjectPool<>(valueLoader, executorService);
    }

    @Override
    public V borrow(final K key, final Callable<? extends V> valueLoader) throws ExecutionException {
        checkNotNull(key);
        checkNotNull(valueLoader);

        final List<V> collection = multimap.get(key);
        synchronized (multimap) {
            if (collection.isEmpty()) {
                try {
                    return executorService.submit(valueLoader).get();
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

    private static class LoadingSynchronizedKeyedObjectPool<K, V>
            extends SynchronizedKeyedObjectPool<K, V>
            implements LoadingKeyedObjectPool<K, V> {
        private final PoolLoader<K, V> valueLoader;

        private LoadingSynchronizedKeyedObjectPool(final PoolLoader<K, V> valueLoader,
                                                   final ListeningExecutorService executorService) {
            super(executorService);
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
