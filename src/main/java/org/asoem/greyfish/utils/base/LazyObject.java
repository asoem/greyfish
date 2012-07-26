package org.asoem.greyfish.utils.base;

import com.google.common.base.Supplier;

/**
 * User: christoph
 * Date: 26.07.12
 * Time: 10:41
 */
public interface LazyObject<T> extends Supplier<T> {
    /**
     * Updates the value for this {@code LazyObject} if {@link #isOutdated()} returns {@code true}
     */
    void updateValue();

    /**
     *
     * @return {@code true} if the value for this {@code LazyObject} is not up to date
     * and should get updated, {@code false} otherwise.
     */
    boolean isOutdated();
}
