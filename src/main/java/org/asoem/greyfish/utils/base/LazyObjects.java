package org.asoem.greyfish.utils.base;

import com.google.common.base.Supplier;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * User: christoph
 * Date: 26.07.12
 * Time: 10:56
 */
public class LazyObjects {

    public static <T> LazyObject<T> synchronizedLazyObject(final LazyObject<T> delegate) {
        return new LazyObject<T>() {
            @Override
            public void updateValue() {
                synchronized (delegate) {
                    delegate.updateValue();
                }
            }

            @Override
            public boolean isOutdated() {
                synchronized (delegate) {
                    return delegate.isOutdated();
                }
            }

            @Override
            public T get() {
                synchronized (delegate) {
                    return delegate.get();
                }
            }
        };
    }

    public static <T> LazyObject<T> computeOnce(Supplier<T> supplier) {
        return new LazyObjectImpl<T>(supplier, UpdateRequests.updateOnce());
    }

    public static void main(String[] args) {

    }
}
