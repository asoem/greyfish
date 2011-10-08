package org.asoem.greyfish.lang;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import org.asoem.greyfish.core.space.KDTree;

import java.util.Map;
import java.util.Set;

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
}
