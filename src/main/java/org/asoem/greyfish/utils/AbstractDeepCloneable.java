package org.asoem.greyfish.utils;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import javolution.util.FastMap;

public abstract class AbstractDeepCloneable<T extends AbstractDeepCloneable<T>> implements DeepClonable {

    protected AbstractDeepCloneable(T clonable, CloneMap map) {
        map.put(clonable, this);
    }

    protected static class CloneMap extends FastMap<AbstractDeepCloneable, AbstractDeepCloneable> {
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
    protected static <E extends DeepClonable> E deepClone(E component, CloneMap map) {
        // There must not exist any implementation of DeepClonable which doesn't extend DeepCloneable
        return (component != null) ? (E) ((AbstractDeepCloneable)component).deepClone(map) : null;
    }

    @SuppressWarnings("unchecked")
    protected static <E extends DeepClonable> Iterable<E> deepCloneAll(Iterable<E> components, final CloneMap map) {
        return Iterables.transform(components, new Function<E, E>() { public E apply(E e) {
            return (e != null) ? (E) ((AbstractDeepCloneable)e).deepClone(map) : null;
        }});
    }
}
