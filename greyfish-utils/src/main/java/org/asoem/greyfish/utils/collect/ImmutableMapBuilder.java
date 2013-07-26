package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * User: christoph
 * Date: 07.10.11
 * Time: 19:08
 */
public class ImmutableMapBuilder<K,V> extends ImmutableMap.Builder<K,V> {

    public static <K,V> ImmutableMapBuilder<K,V> newInstance() {
        return new ImmutableMapBuilder<K, V>();
    }

    public <E> ImmutableMapBuilder<K,V> putAll(final Iterable<? extends E> iterable, final Function<? super E,K> keyFunction, final Function<? super E,V> valueFunction) {

        for (final E e : iterable) {
            put(keyFunction.apply(e), valueFunction.apply(e));
        }

        return this;
    }

    @Override
    public ImmutableMapBuilder<K, V> put(final K key, final V value) {
        super.put(key, value);
        return this;
    }

    @Override
    public ImmutableMapBuilder<K, V> putAll(final Map<? extends K, ? extends V> map) {
        super.putAll(map);
        return this;
    }
}
