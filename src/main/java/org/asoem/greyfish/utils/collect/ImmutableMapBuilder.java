package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import java.util.List;
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

    public <E> ImmutableMapBuilder<K,V> putAll(Iterable<? extends E> iterable, Function<? super E,K> keyFunction, Function<? super E,V> valueFunction) {

        for (E e : iterable) {
            put(keyFunction.apply(e), valueFunction.apply(e));
        }

        return this;
    }

    @Override
    public ImmutableMapBuilder<K, V> put(K key, V value) {
        super.put(key, value);
        return this;
    }

    @Override
    public ImmutableMapBuilder<K, V> putAll(Map<? extends K, ? extends V> map) {
        super.putAll(map);
        return this;
    }

    public static <E, K> ImmutableMap<K, Integer> uniqueIndex(List<E> list, Function<E, K> function) {
        final ImmutableMap.Builder<K, Integer> builder = ImmutableMap.builder();
        int i = 0;
        for (E elem : list) {
            builder.put(function.apply(elem), i++);
        }
        return builder.build();
    }
}
