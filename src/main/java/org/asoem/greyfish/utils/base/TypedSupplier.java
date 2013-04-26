package org.asoem.greyfish.utils.base;

import com.google.common.base.Supplier;
import com.google.common.reflect.TypeToken;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 26.04.13
 * Time: 13:39
 */
public interface TypedSupplier<T> extends Supplier<T> {

    /**
     * @return the type of the value this {@code Supplier} is supplying
     */
    TypeToken<T> getValueType();

    @Nullable
    @Override
    T get();
}
