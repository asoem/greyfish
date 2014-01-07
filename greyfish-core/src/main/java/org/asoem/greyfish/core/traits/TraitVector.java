package org.asoem.greyfish.core.traits;

import org.asoem.greyfish.utils.base.TypedSupplier;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class TraitVector<T> implements TypedSupplier<T> {
    @Nullable
    private final T value;
    private final String name;

    private TraitVector(@Nullable final T value, final String name) {
        this.value = value;
        this.name = name;
    }

    @Nullable
    @Override
    public T get() {
        return value;
    }

    public static <T> TraitVector<T> of(final String name, @Nullable final T value) {
        checkNotNull(name);
        return new TraitVector<>(value, name);
    }

    public String getName() {
        return name;
    }

}
