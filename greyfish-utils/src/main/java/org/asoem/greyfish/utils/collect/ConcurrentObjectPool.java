package org.asoem.greyfish.utils.collect;

import com.google.common.base.Supplier;
import com.google.common.collect.Queues;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ConcurrentObjectPool<T> implements ObjectPool<T> {

    private final ConcurrentLinkedQueue<T> deque = Queues.newConcurrentLinkedQueue();
    private final Supplier<T> objectFactory;

    public ConcurrentObjectPool(final Supplier<T> objectFactory) {
        this.objectFactory = checkNotNull(objectFactory);
    }

    @Override
    public T borrow() {
        @Nullable
        T poll = deque.poll();

        if (poll == null) {
            poll = objectFactory.get();
        }

        return poll;
    }

    @Override
    public void release(final T object) {
        deque.offer(object);
    }
}
