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
