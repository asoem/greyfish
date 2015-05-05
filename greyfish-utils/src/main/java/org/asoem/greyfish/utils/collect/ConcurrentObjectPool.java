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

import com.google.common.base.Throwables;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.UncheckedExecutionException;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An object pool implementation backed by a {@link ConcurrentLinkedQueue}. Therefore, this class does not permit the
 * use of {@code null} elements.
 *
 * @param <T> The type of the objects the pool provides
 */
public class ConcurrentObjectPool<T> implements ObjectPool<T> {

    private final ConcurrentLinkedQueue<T> deque = Queues.newConcurrentLinkedQueue();
    private final ExecutorService executorService;

    private ConcurrentObjectPool(final ExecutorService executorService) {
        this.executorService = executorService;
    }

    public static <T> ConcurrentObjectPool<T> create() {
        final ListeningExecutorService executorService = MoreExecutors.newDirectExecutorService();
        return create(executorService);
    }

    public static <T> ConcurrentObjectPool<T> create(final ExecutorService executorService) {
        return new ConcurrentObjectPool<>(checkNotNull(executorService));
    }

    public static <T> ConcurrentLoadingObjectPool<T> create(final Callable<T> valueLoader) {
        return create(valueLoader, MoreExecutors.newDirectExecutorService());
    }

    public static <T> ConcurrentLoadingObjectPool<T> create(final Callable<T> valueLoader,
                                                            final ExecutorService executorService) {
        return new ConcurrentLoadingObjectPool<>(checkNotNull(valueLoader), checkNotNull(executorService));
    }

    @Override
    public T borrow(final Callable<T> valueLoader) throws ExecutionException {
        checkNotNull(valueLoader);

        @Nullable
        T poll = deque.poll();

        if (poll == null) { // deque was empty
            try {
                poll = executorService.submit(valueLoader).get();
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                if (cause instanceof Error) {
                    throw new ExecutionError((Error) cause);
                } else if (cause instanceof RuntimeException) {
                    throw new UncheckedExecutionException(cause);
                }
                throw ee;
            } catch (InterruptedException e) {
                throw Throwables.propagate(e);
            }
        }

        return poll;
    }

    @Override
    public void release(final T object) {
        deque.offer(checkNotNull(object));
    }

    /**
     * A loading object pool implementation backed by a {@link ConcurrentLinkedQueue}.
     *
     * @param <T> The type of the objects the pool provides
     */
    public static class ConcurrentLoadingObjectPool<T> extends ConcurrentObjectPool<T> implements LoadingObjectPool<T> {

        private final Callable<T> valueLoader;

        private ConcurrentLoadingObjectPool(final Callable<T> valueLoader, final ExecutorService executorService) {
            super(executorService);
            this.valueLoader = valueLoader;
        }

        @Override
        public T borrow() throws ExecutionException {
            return borrow(valueLoader);
        }

    }
}
