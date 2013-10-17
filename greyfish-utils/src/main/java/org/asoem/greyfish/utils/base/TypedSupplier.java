package org.asoem.greyfish.utils.base;

import com.google.common.base.Supplier;
import com.google.common.reflect.TypeToken;

public interface TypedSupplier<T> extends Supplier<T> {

    /**
     * @return the {@code TypeToken} for the type of this {@code TypedSupplier}
     */
    TypeToken<T> getValueType();
}
