package org.asoem.greyfish.core.genes;

import com.google.common.base.Supplier;

/**
 * User: christoph
 * Date: 05.06.12
 * Time: 10:15
 */
public interface Trait<T> extends Supplier<T> {
    /**
     * Set the new value for this {@code Trait}
     *
     * @param value the new value
     */
    void set(T value);
}
