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
