package org.asoem.greyfish.utils.base;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

public abstract class Memoizer<T> implements Supplier<T> {
    private Optional<T> value = Optional.absent();

    @Override
    public final T get() {
        if (!isValid(value)) {
            synchronized (this) {
                if (!isValid(value)) {
                    T t = delegate().get();
                    value = Optional.fromNullable(t);
                    loaded(t);
                    return t;
                }
            }
        }
        return value.orNull();
    }

    /**
     * This method will be called if the memoizer has loaded and stored the value {@code t} to memoize.
     *
     * @param t the new memoized value
     */
    protected void loaded(final T t) {}

    protected abstract Supplier<T> delegate();

    /**
     * Check if given {@code memoized} value is valid. <p>If this method returns {@code false} than the memoizer will
     * update it's memoized value using {@link #delegate()}</p>
     *
     * @param memoized the current memoized value which is {@link com.google.common.base.Optional#absent() absent} if
     *                 the memoizer has not been initialized yet.
     * @return {@code true} if the memoizer holds a valid value, {@code false} otherwise
     */
    protected abstract boolean isValid(final Optional<T> memoized);
}
