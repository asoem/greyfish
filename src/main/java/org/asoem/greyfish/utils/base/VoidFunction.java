package org.asoem.greyfish.utils.base;

import com.google.common.base.Function;

import javax.annotation.Nullable;

public abstract class VoidFunction<T> implements Function<T, Void> {
    public Void apply(@Nullable T t) {
        process(t);
        return null;
    }

    protected abstract void process(@Nullable T t);
}
