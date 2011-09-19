package org.asoem.greyfish.utils;

import com.google.common.base.Function;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class CloneMap extends ForwardingMap<DeepCloneable, DeepCloneable> {

    private final Map<DeepCloneable, DeepCloneable> map = Maps.newHashMap();
    private DeepCloneable keyForExpectedPut = null;

    public <T extends DeepCloneable, E> E clone(@Nullable T cloneable, Class<E> clazz) {
        checkNotNull(clazz);
        if (putExpected())
            throw new IllegalStateException("A clone should add an entry for itself first with map.insert(clone, this)");

        if (cloneable == null)
            return null;
        else if (map.containsKey(cloneable)) {
            return clazz.cast(map.get(cloneable));
        }
        else {
            keyForExpectedPut = cloneable;
            return clazz.cast(cloneable.deepCloneHelper(this));
        }
    }

    private boolean putExpected() {
        return keyForExpectedPut != null;
    }

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
        throw new UnsupportedOperationException();
    }

    public <E extends DeepCloneable> void insert(E key, E value) {
        checkNotNull(key);
        checkNotNull(value);
        checkArgument(key == keyForExpectedPut, "Key must be the cloneable of your previous clone(cloneable, clone)");
        checkArgument(key.getClass().equals(value.getClass()), "Class of ");
        keyForExpectedPut = null;
        super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends DeepCloneable, ? extends DeepCloneable> map) {
        throw new UnsupportedOperationException();
    }

    public <T extends DeepCloneable> Iterable<T> cloneAll(Iterable<? extends T> iterable, final Class<T> clazz) {
        checkNotNull(clazz);
        return Iterables.transform(iterable, new Function<T, T>() {
            @Override
            public T apply(T cloneable) {
                return CloneMap.this.clone(cloneable, clazz);
            }
        });
    }

    public static <E extends DeepCloneable> E deepClone(@Nullable DeepCloneable cloneable, Class<E> clazz) {
        checkNotNull(clazz);
        return new CloneMap().clone(cloneable, clazz);
    }
}
