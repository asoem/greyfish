package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.utils.base.TypedSupplier;

/**
 * User: christoph
 * Date: 05.06.12
 * Time: 10:15
 */
public interface Trait<T> extends TypedSupplier<T> {
    /**
     * Set the new value for this {@code Trait}
     *
     * @param value the new value
     */
    void set(T value);

    void setFromSupplier(TypedSupplier<?> supplier);
}
