package org.asoem.greyfish.utils.base;

import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@code CycleCloner} is used to make deep copies of cyclic {@link DeepCloneable} object graphs.
 *
 * Cloning starts by calling the static {@link #clone(DeepCloneable)} method with the object to clone.
 * It calls the {@link DeepCloneable#deepClone(DeepCloner)} method of the passed object.
 * This method should then call a special "Cloner"-Constructor where you must first add the cloneable x clone pair
 * to this cloner using the {@link #addClone(DeepCloneable, DeepCloneable)} method and then
 * clone all {@code DeepCloneable} fields using the {@link #getClone(DeepCloneable)} method.
 */
public class CycleCloner implements DeepCloner {

    private final Map<DeepCloneable, DeepCloneable> map = Maps.newIdentityHashMap();

    private CycleCloner() {}

    @Override
    public <T extends DeepCloneable> void addClone(final T original, final T clone) {
        checkNotNull(original);
        checkNotNull(clone);
        checkArgument(original.getClass() == clone.getClass(),
                "Classes of original and clone don't match: {} != {}", original.getClass(), clone.getClass());
        map.put(original, clone);
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked") // safe
    public <T extends DeepCloneable> T getClone(@Nullable final T cloneable) {
        if (cloneable == null) {
            return null;
        }

        final DeepCloneable result = map.containsKey(cloneable)
                ? map.get(cloneable)
                : cloneable.deepClone(this);
        return ((Class<T>) cloneable.getClass()).cast(result);
    }

    /**
     * Create a deep clone of given {@code cloneable}
     *
     * @param cloneable the object to make a deep clone of
     * @param <T> the type of {@code cloneable}
     * @return a deep clone of {@code cloneable}
     */
    public static <T extends DeepCloneable> T clone(@Nullable final T cloneable) {
        return cloneable == null ? null : new CycleCloner().getClone(cloneable);
    }

    /**
     * Create a new {@code CycleCloner} instance
     * @return a new {@code CycleCloner} instance
     */
    public static CycleCloner create() {
        return new CycleCloner();
    }
}
