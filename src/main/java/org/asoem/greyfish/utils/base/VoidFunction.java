package org.asoem.greyfish.utils.base;

import javax.annotation.Nullable;

public interface VoidFunction<T> {
    public void apply(@Nullable T t);
}
