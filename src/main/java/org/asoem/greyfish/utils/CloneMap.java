package org.asoem.greyfish.utils;

import com.google.common.base.Function;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.asoem.greyfish.core.conditions.GFCondition;

import java.util.List;
import java.util.Map;

public class CloneMap extends ForwardingMap<DeepClonable, DeepClonable> {

    private Map<DeepClonable, DeepClonable> map = Maps.newHashMap();

    public <T extends DeepClonable> T clone(T clonable, Class<T> clazz) {
        if (clonable == null)
            return null;
        else if (map.containsKey(clonable))
            return clazz.cast(map.get(clonable));
        else
            return clazz.cast(clonable.deepCloneHelper(this));
    }

    public static CloneMap newInstance() { return new CloneMap(); }

    @Override
    protected Map<DeepClonable, DeepClonable> delegate() {
        return map;
    }

    @Override
    public DeepClonable remove(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DeepClonable put(DeepClonable key, DeepClonable value) {
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends DeepClonable, ? extends DeepClonable> map) {
        super.putAll(map);
    }

    public <T extends DeepClonable> Iterable<T> cloneAll(Iterable<T> conditions, final Class<T> clazz) {
        return Iterables.transform(conditions, new Function<T, T>() {
            @Override
            public T apply(T clonable) {
                return CloneMap.this.clone(clonable, clazz);
            }
        });
    }
}
