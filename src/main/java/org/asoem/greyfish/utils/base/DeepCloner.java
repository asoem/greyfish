package org.asoem.greyfish.utils.base;

import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@code DeepCloner} is used to make deep copies of {@link DeepCloneable} objects.
 *
 * The usual way is to use the static {@link #clone(DeepCloneable, Class)} method to clone a certain object.
 * This method calls the {@link DeepCloneable#deepClone(DeepCloner)} method of the object whose implementation should do nothing but call the "Cloner"-Constructor.
 * Here, the object should pass all fields to the {@link #cloneField(DeepCloneable, Class)} method, which are required for a deep copy.
 */
public class DeepCloner {

    private final Map<DeepCloneable, DeepCloneable> map = Maps.newHashMap();
    private DeepCloneable keyForExpectedPut = null;

    private DeepCloner() {
    }

    private boolean insertIsRequired() {
        return keyForExpectedPut != null;
    }

    public <T extends DeepCloneable> void setAsCloned(T key, T value) {
        checkNotNull(key);
        checkNotNull(value);
        checkArgument(key == keyForExpectedPut,
                "key was expected to be == " + keyForExpectedPut);
        keyForExpectedPut = null;
        map.put(key, value);
    }

    public <T extends DeepCloneable> T cloneField(@Nullable T cloneable, Class<T> clazz) {
        checkNotNull(clazz);
        if (insertIsRequired())
            throw new IllegalStateException(
                    "A clone should add an entry for itself first with cloner.setAsCloned(cloneable, this)");

        if (cloneable == null)
            return null;
        else if (map.containsKey(cloneable)) {
            return clazz.cast(map.get(cloneable));
        }
        else {
            keyForExpectedPut = cloneable;
            return clazz.cast(cloneable.deepClone(this));
        }
    }

    public static <T extends DeepCloneable> T clone(@Nullable T cloneable, Class<T> clazz) {
        checkNotNull(clazz);
        return new DeepCloner().cloneField(cloneable, clazz);
    }
}
