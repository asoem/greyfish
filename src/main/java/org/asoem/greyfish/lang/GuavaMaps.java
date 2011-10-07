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
public class GuavaMaps {

    public static <K,V> Map<K,V> fromKeys(Set<? extends K> keys, Function<? super K, V> valueFunction) {
        ImmutableMap.Builder<K,V> builder = ImmutableMap.builder();
        for (K key : keys) {
            builder.put(key, valueFunction.apply(key));
        }
        return builder.build();
    }
}
