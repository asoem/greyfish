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

package org.asoem.greyfish.utils.collect;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * A interface for implementing object pools (http://en.wikipedia.org/wiki/Object_pool_pattern).
 *
 * @param <T> The type of the objects the pool provides
 */
public interface ObjectPool<T> {
    /**
     * Borrow an object from this pool. If the pool is empty a new object is created using the {@code valueLoader}.
     *
     * @param valueLoader the factory for new objects
     * @return an object borrowed from the pool, or a newly created one from {@code valueLoader}
     * @throws ExecutionException
     */
    T borrow(Callable<T> valueLoader) throws ExecutionException;

    /**
     * Release an element
     *
     * @param object the object to release
     */
    void release(T object);
}
