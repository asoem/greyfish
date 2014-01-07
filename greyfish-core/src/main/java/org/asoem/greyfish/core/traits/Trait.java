package org.asoem.greyfish.core.traits;

import org.asoem.greyfish.utils.base.TypedSupplier;

public interface Trait<T> extends TypedSupplier<T> {
    /**
     * Set the new value for this {@code Trait}
     *
     * @param value the new value
     */
    void set(T value);

    /**
     * Copy the value from the given {@code TypedSupplier}
     * if the type of this {@code Trait} is assignable from the type of given {@code supplier},
     * otherwise an {@link IllegalArgumentException} is thrown.
     * @param supplier the supplier which holds the value to copy.
     */
    void copyFrom(TypedSupplier<?> supplier);

    @Override
    T get();
}
