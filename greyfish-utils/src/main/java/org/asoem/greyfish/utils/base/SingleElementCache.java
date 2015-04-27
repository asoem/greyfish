/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.utils.base;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A single element cache is a memoizer of an object of type {@code T}. <p>The cache can be {@link #invalidate()
 * invalidated} so that it will reload the memoized value on the next call to {@link #get()}</p>
 *
 * @param <T> the type of the element to supply
 */
@ThreadSafe
public final class SingleElementCache<T> extends Memoizer<T> {

    private final Supplier<T> delegate;
    private transient volatile boolean valid = false;

    private SingleElementCache(final Supplier<T> delegate) {
        this.delegate = delegate;
    }

    public void invalidate() {
        valid = false;
    }

    /**
     * Create a new {@code SingleElementCache} which computes it's memoized value using {@code delegate}.
     *
     * @param delegate the value supplier
     * @param <T>      the type of the element to cache
     * @return a new cache
     */
    public static <T> SingleElementCache<T> memoize(final Supplier<T> delegate) {
        return new SingleElementCache<T>(delegate);
    }

    protected Supplier<T> delegate() {
        return delegate;
    }

    @Override
    protected boolean isValid(final Optional<T> memoized) {
        return valid;
    }

    boolean isInvalid() {
        return !valid;
    }

    @Override
    protected void loaded(final T t) {
        valid = true;
    }
}
