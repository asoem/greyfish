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

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.UncheckedExecutionException;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConcurrentObjectPool<T> implements ObjectPool<T> {

    private final ConcurrentLinkedQueue<T> deque = Queues.newConcurrentLinkedQueue();

    private ConcurrentObjectPool() {
    }

    public static <T> ConcurrentObjectPool<T> create() {
        return new ConcurrentObjectPool<T>();
    }

    public static <T> ConcurrentLoadingObjectPool<T> create(final Callable<T> valueLoader) {
        return new ConcurrentLoadingObjectPool<T>(checkNotNull(valueLoader));
    }

    @Override
    public T borrow(final Callable<T> objectFactory) throws ExecutionException {
        checkNotNull(objectFactory);

        @Nullable
        T poll = deque.poll();

        if (poll == null) {
            try {
                poll = objectFactory.call();
            } catch (Exception e) {
                throw new ExecutionException(e);
            } catch (Error e) {
                throw new ExecutionError(e);
            } catch (Throwable e) {
                throw new UncheckedExecutionException(e);
            }
        }

        return poll;
    }

    @Override
    public void release(final T object) {
        deque.offer(object);
    }

    public static class ConcurrentLoadingObjectPool<T> extends ConcurrentObjectPool<T> implements LoadingObjectPool<T> {

        private final Callable<T> objectFactory;

        private ConcurrentLoadingObjectPool(final Callable<T> objectFactory) {
            this.objectFactory = objectFactory;
        }

        @Override
        public T borrow() throws ExecutionException {
            return borrow(objectFactory);
        }
    }
}
