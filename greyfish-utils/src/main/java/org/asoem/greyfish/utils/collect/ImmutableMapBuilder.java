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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * @deprecated
 */
@Deprecated
public final class ImmutableMapBuilder<K, V> extends ImmutableMap.Builder<K, V> {

    /**
     * @deprecated
     */
    @Deprecated
    public static <K, V> ImmutableMapBuilder<K, V> newInstance() {
        return new ImmutableMapBuilder<K, V>();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public <E> ImmutableMapBuilder<K, V> putAll(final Iterable<? extends E> iterable,
                                                final Function<? super E, K> keyFunction,
                                                final Function<? super E, V> valueFunction) {
        checkNotNull(iterable);
        checkNotNull(keyFunction);
        checkNotNull(valueFunction);

        for (final E e : iterable) {
            final K key = checkNotNull(keyFunction.apply(e));
            final V value = checkNotNull(valueFunction.apply(e));
            put(key, value);
        }

        return this;
    }

    /**
     * @deprecated
     */
    @Deprecated
    @Override
    public ImmutableMapBuilder<K, V> put(final K key, final V value) {
        super.put(key, value);
        return this;
    }

    /**
     * @deprecated
     */
    @Deprecated
    @Override
    public ImmutableMapBuilder<K, V> putAll(final Map<? extends K, ? extends V> map) {
        super.putAll(map);
        return this;
    }
}
