package org.asoem.greyfish.utils;

public abstract class AbstractDeepCloneable implements DeepClonable {

    protected AbstractDeepCloneable(AbstractDeepCloneable clonable, CloneMap map) {
        map.put(clonable, this);
    }

    protected AbstractDeepCloneable() { }

    @Override
    final public <E extends DeepClonable> E deepClone(Class<E> clazz) {
        return CloneMap.newInstance().clone(clazz.cast(this), clazz);
    }
//
//    public <E extends DeepClonable> E deepClone(CloneMap map, Class<E> clazz) {
//        assert map != null : "map must not be null";
//
//        if (map.containsKey(this))
//            return clazz.cast(map.get(this));
//        else {
//            return clazz.cast(deepCloneHelper(map));
//        }
//    }

//    public static <E extends DeepClonable> E deepClone(E component, Class<E> clazz) {
//        return clazz.cast(component.deepCloneHelper(CloneMap.newInstance()));
//    }
//
//    protected static <E extends DeepClonable> E deepClone(E component, CloneMap map, Class<E> clazz) {
//        return (component != null) ? component.deepClone(map, clazz) : null;
//    }
//
//    protected static <E extends DeepClonable> Iterable<E> deepCloneAll(Iterable<E> components, final CloneMap map, final Class<E> clazz) {
//        return Iterables.transform(components, new Function<E, E>() { public E apply(E e) {
//            return (e != null) ? e.deepClone(map, clazz) : null;
//        }});
//    }
}
