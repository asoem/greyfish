package org.asoem.greyfish.utils.base;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A single element cache is a memoizer of an object of type {@code T}. <p>The cache can be {@link #invalidate()
 * invalidated} so that it will reload the memoized value on the next call to {@link #get()}</p>
 *
 * @param <T> the type of the element to supply
 */
@ThreadSafe
public final class SingleElementCache<T> extends Memoizer<T> {

    private final Supplier<T> delegate;
    private transient volatile boolean valid = false;

    private SingleElementCache(final Supplier<T> delegate) {
        this.delegate = delegate;
    }

    public void invalidate() {
        valid = false;
    }

    /**
     * Create a new {@code SingleElementCache} which computes it's memoized value using {@code delegate}.
     *
     * @param delegate the value supplier
     * @param <T>      the type of the element to cache
     * @return a new cache
     */
    public static <T> SingleElementCache<T> memoize(final Supplier<T> delegate) {
        return new SingleElementCache<T>(delegate);
    }

    protected Supplier<T> delegate() {
        return delegate;
    }

    @Override
    protected boolean isValid(final Optional<T> memoized) {
        return valid;
    }

    boolean isInvalid() {
        return !valid;
    }

    @Override
    protected void loaded(final T t) {
        valid = true;
    }
}
