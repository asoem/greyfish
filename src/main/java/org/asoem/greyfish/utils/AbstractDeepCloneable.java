package org.asoem.greyfish.utils;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractDeepCloneable implements DeepClonable {

    protected AbstractDeepCloneable() { }

    final public DeepClonable deepClone() {
        return deepClone(new HashMap<AbstractDeepCloneable, AbstractDeepCloneable>());
    }

    protected DeepClonable deepClone(Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        Preconditions.checkNotNull(mapDict);

        if (mapDict.containsKey(this))
            return mapDict.get(this);
        else {
            AbstractDeepCloneable ret = deepCloneHelper(mapDict);
            mapDict.put(this, ret);
            return ret;
        }
    }

    ///Classes should override this method
    ///with the following code:
    /// return new NameOfMyClass(this, mapDict);
    protected abstract AbstractDeepCloneable deepCloneHelper(Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict);

    @SuppressWarnings("unchecked")
    protected static <T extends DeepClonable> T deepClone(T component, Map<AbstractDeepCloneable, AbstractDeepCloneable> map) {
        // There must not exist any implementation of DeepClonable which doesn't extend DeepCloneable
        return (component != null) ? (T) ((AbstractDeepCloneable)component).deepClone(map) : null;
    }

    protected static <T extends DeepClonable> Iterable<T> deepClone(Iterable<T> components, final Map<AbstractDeepCloneable, AbstractDeepCloneable> map) {
        return Iterables.transform(components, new Function<T, T>() { public T apply(T e) {return deepClone(e, map);}});
    }
}
