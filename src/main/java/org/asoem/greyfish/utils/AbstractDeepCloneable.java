package org.asoem.greyfish.utils;

public abstract class AbstractDeepCloneable implements DeepCloneable {

    protected AbstractDeepCloneable(DeepCloneable cloneable, CloneMap map) {
        map.put(cloneable, this);
    }

    protected AbstractDeepCloneable() { }

    @Override
    public final <E extends DeepCloneable> E deepClone(Class<E> clazz) {
        return CloneMap.newInstance().clone(clazz.cast(this), clazz);
    }
//
//    public <E extends DeepCloneable> E deepClone(CloneMap map, Class<E> clazz) {
//        assert map != null : "map must not be null";
//
//        if (map.containsKey(this))
//            return clazz.cast(map.get(this));
//        else {
//            return clazz.cast(deepCloneHelper(map));
//        }
//    }

//    public static <E extends DeepCloneable> E deepClone(E component, Class<E> clazz) {
//        return clazz.cast(component.deepCloneHelper(CloneMap.newInstance()));
//    }
//
//    protected static <E extends DeepCloneable> E deepClone(E component, CloneMap map, Class<E> clazz) {
//        return (component != null) ? component.deepClone(map, clazz) : null;
//    }
//
//    protected static <E extends DeepCloneable> Iterable<E> deepCloneAll(Iterable<E> components, final CloneMap map, final Class<E> clazz) {
//        return Iterables.transform(components, new Function<E, E>() { public E apply(E e) {
//            return (e != null) ? e.deepClone(map, clazz) : null;
//        }});
//    }
}
