package org.asoem.greyfish.utils.base;

import com.google.common.base.Supplier;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * User: christoph
 * Date: 26.07.12
 * Time: 10:56
 */
public class LazyObjects {
    public static <T> LazyObject<T> threadSave(final LazyObject<T> delegate) {
        return new LazyObject<T>() {

            private final ReadWriteLock lock = new ReentrantReadWriteLock();

            @Override
            public void updateValue() {
                if (isOutdated()) {
                    lock.readLock().unlock();
                    lock.writeLock().lock();
                    try {
                        delegate.updateValue();
                    } finally {
                        lock.writeLock().unlock();
                        lock.readLock().lock();
                    }
                }
            }

            @Override
            public boolean isOutdated() {
                return delegate.isOutdated();
            }

            @Override
            public T get() {
                lock.readLock().lock();
                try {
                    updateValue();
                    return delegate.get();
                }
                finally {
                    lock.readLock().unlock();
                }
            }
        };
    }

    public static <T> LazyObject<T> computeOnce(Supplier<T> supplier) {
        return new LazyObjectImpl<T>(supplier, UpdateRequests.updateOnce());
    }
}
