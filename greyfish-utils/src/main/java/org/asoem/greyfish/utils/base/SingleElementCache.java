package org.asoem.greyfish.utils.base;

import com.google.common.base.Supplier;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A single element cache is a supplier of an object of type {@code T}.
 * This value has an associated context which makes it valid or invalid.
 * @param <T> the type of the element to supply
 */
public class SingleElementCache<T> implements Supplier<T> {

    private final Supplier<T> delegate;
    private final AtomicReference<CacheState> state =
            new AtomicReference<CacheState>(CacheState.INVALID);
    private T value;

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

    /**
     * Create a new {@code SingleElementCache} which reloads it's supplying element,
     * if it is in an invalid state. Therefore the {@link #get()} operation will always return a valid element.
     * @param delegate the reload strategy
     * @param <T> the type of the element to cache
     * @return a new cache
     */
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
