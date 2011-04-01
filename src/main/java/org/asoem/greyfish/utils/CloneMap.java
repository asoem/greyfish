package org.asoem.greyfish.utils;

import com.google.common.base.Function;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import java.util.Map;

public class CloneMap extends ForwardingMap<DeepCloneable, DeepCloneable> {

    private final Map<DeepCloneable, DeepCloneable> map = Maps.newHashMap();

    public <T extends DeepCloneable, E> E clone(T clonable, Class<E> clazz) {
        if (clonable == null)
            return null;
        else if (map.containsKey(clonable))
            return clazz.cast(map.get(clonable));
        else
            return clazz.cast(clonable.deepCloneHelper(this));
    }

    public static CloneMap newInstance() { return new CloneMap(); }

    @Override
    protected Map<DeepCloneable, DeepCloneable> delegate() {
        return map;
    }

    @Override
    public DeepCloneable remove(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DeepCloneable put(DeepCloneable key, DeepCloneable value) {
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends DeepCloneable, ? extends DeepCloneable> map) {
        super.putAll(map);
    }

    public <T extends DeepCloneable> Iterable<T> cloneAll(Iterable<T> conditions, final Class<T> clazz) {
        return Iterables.transform(conditions, new Function<T, T>() {
            @Override
            public T apply(T clonable) {
                return CloneMap.this.clone(clonable, clazz);
            }
        });
    }
}
