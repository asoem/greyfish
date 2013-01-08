package org.asoem.greyfish.utils.base;

import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@code DeepCloner} is used to make deep copies of {@link DeepCloneable} objects.
 *
 * The intended way is to call the static {@link #clone(DeepCloneable)} method to clone a certain object.
 * This method calls the {@link DeepCloneable#deepClone(CloneMap)} method of the passed object.
 * This method should then call a special "Cloner"-Constructor where you must first add the cloneable x clone pair
 * to this cloner using the {@link #addClone(DeepCloneable, DeepCloneable)} method and then
 * clone all {@code DeepCloneable} fields using the {@link #getClone(DeepCloneable)} method.
 */
public class DeepCloner implements CloneMap {

    private final Map<DeepCloneable, DeepCloneable> map = Maps.newIdentityHashMap();

    private DeepCloner() {}

    /**
     * Register {@code clone} as the clone of {@code original}.
     * This method must be called in the (super)constructor of {@code clone} before any of the fields get cloned using this cloner.
     *
     * @param original a cloneable
     * @param clone the clone of original
     */
    @Override
    public <T extends DeepCloneable> void addClone(T original, T clone) {
        checkNotNull(original);
        checkNotNull(clone);
        checkArgument(original.getClass() == clone.getClass(),
                "Classes of original and clone don't match: {} != {}", original.getClass(), clone.getClass());
        map.put(original, clone);
    }

    /**
     * Get the deep clone of {@code cloneable} casted to {@code clazz}.
     *
     * @param cloneable the object to clone
     * @param clazz the class of {@code cloneable}
     * @return the deep clone of {@code cloneable}, {@code null} if {@code cloneable} is {@code null}
     */
    @Override
    @Nullable
    public <T extends DeepCloneable> T getClone(@Nullable T cloneable, Class<T> clazz) {
        checkNotNull(clazz, "argument 'clazz' must not be null");
        return clazz.cast(getClone(cloneable));
    }

    /**
     * Get the deep clone of {@code cloneable}
     *
     * @param cloneable the object to clone
     * @return the deep clone of {@code cloneable}, {@code null} if {@code cloneable} is {@code null}
     */
    @Override
    @Nullable
    public DeepCloneable getClone(@Nullable DeepCloneable cloneable) {
        return getClone(cloneable, this);
    }

    /**
     * Get the deep clone of {@code cloneable} using the given {@code CloneMap} for caching.
     * This method Enables decoration of this class.
     *
     * @param cloneable the object to clone
     * @param cloneMap the {@code CloneMap} to pass to {@link DeepCloneable#deepClone(CloneMap)}
     * @return the deep clone of {@code cloneable}, {@code null} if {@code cloneable} is {@code null}
     */
    public DeepCloneable getClone(DeepCloneable cloneable, CloneMap cloneMap) {
        if (cloneable == null) {
            return null;
        }
        else if (map.containsKey(cloneable)) {
            return map.get(cloneable);
        }
        else {
            return cloneable.deepClone(cloneMap);

            /*
            if (!map.containsKey(cloneable))
                throw new IllegalStateException("Missing map entry: Did you forget to call DeepCloner.addClone() in constructor of " + cloneable.getClass());
            if (clone != map.get(cloneable))
                throw new IllegalStateException("Clone in map for cloneable is not the same as clone returned by cloneable.deepClone()");
            */
        }
    }


    /**
     * Create a deep clone of {@code cloneable} casted to {@code clazz}
     *
     * @param cloneable the object to make a deep clone of
     * @param clazz the class of {@code cloneable}
     * @param <T> the type of {@code cloneable}
     * @return a deep clone of {@code cloneable}
     */
    public static <T extends DeepCloneable> T clone(@Nullable T cloneable, Class<T> clazz) {
        checkNotNull(clazz);
        return new DeepCloner().getClone(cloneable, clazz);
    }

    /**
     * Create a deep clone of {@code cloneable}
     *
     * @param cloneable the object to make a deep clone of
     * @return a deep clone of {@code cloneable}
     */
    public static DeepCloneable clone(@Nullable DeepCloneable cloneable) {
        return new DeepCloner().getClone(cloneable);
    }

    public static DeepCloner newInstance() {
        return new DeepCloner();
    }
}
