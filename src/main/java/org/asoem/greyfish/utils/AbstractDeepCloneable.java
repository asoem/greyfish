package org.asoem.greyfish.utils;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import javolution.util.FastMap;
import org.asoem.greyfish.core.individual.AbstractGFComponent;

public abstract class AbstractDeepCloneable<T extends AbstractDeepCloneable<T>> implements DeepClonable {

    protected AbstractDeepCloneable(AbstractGFComponent clonable, CloneMap map) {
        map.put(clonable, this);
    }

    protected class CloneMap extends FastMap<AbstractDeepCloneable, AbstractDeepCloneable> {
         private CloneMap() {}
    }

    protected AbstractDeepCloneable() { }

    final public T deepClone() {
        return deepClone(new CloneMap());
    }

    @SuppressWarnings("unchecked")
    protected T deepClone(CloneMap map) {
        Preconditions.checkNotNull(map);

        if (map.containsKey(this))
            return (T) map.get(this);
        else {
            return deepCloneHelper(map);
        }
    }

    ///Classes should override this method
    ///with the following code:
    /// return new NameOfMyClass(this, mapDict);
    protected abstract T deepCloneHelper(CloneMap map);

    @SuppressWarnings("unchecked")
    protected static <T extends DeepClonable> T deepClone(T component, CloneMap map) {
        // There must not exist any implementation of DeepClonable which doesn't extend DeepCloneable
        return (component != null) ? (T) ((AbstractDeepCloneable)component).deepClone(map) : null;
    }

    protected static <T extends DeepClonable> Iterable<T> deepClone(Iterable<T> components, final CloneMap map) {
        return Iterables.transform(components, new Function<T, T>() { public T apply(T e) {return deepClone(e, map);}});
    }
}
