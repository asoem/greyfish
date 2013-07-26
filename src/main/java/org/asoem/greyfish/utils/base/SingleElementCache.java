package org.asoem.greyfish.utils.base;

import com.google.common.base.Supplier;

import java.util.concurrent.atomic.AtomicReference;

/**
 * User: christoph
 * Date: 05.04.13
 * Time: 11:41
 */
public class SingleElementCache<T> implements Supplier<T> {

    private final Supplier<T> delegate;
    private transient T value;
    private transient AtomicReference<CacheState> state = new AtomicReference<CacheState>(CacheState.INVALID);

    private SingleElementCache(final Supplier<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public T get() {
        if (state.get() != CacheState.VALID) {
            update();
        }
        return value;
    }

    public void update() {
        synchronized (this) {
            while (state.compareAndSet(CacheState.INVALID, CacheState.UPDATING)) {
                value = delegate.get();
                this.state.compareAndSet(CacheState.UPDATING, CacheState.VALID);
            }
        }
    }

    public void invalidate() {
        state.set(CacheState.INVALID);
    }

    public static <T> SingleElementCache<T> memoize(final Supplier<T> delegate) {
        return new SingleElementCache<T>(delegate);
    }

    public boolean isInvalid() {
        return state.get() != CacheState.VALID;
    }

    private static enum CacheState {
        VALID,
        INVALID,
        UPDATING
    }
}
