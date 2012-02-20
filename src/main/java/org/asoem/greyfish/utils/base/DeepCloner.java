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
 * Here, the object should pass evaluates fields to the {@link #cloneField(DeepCloneable, Class)} method, which are required for a deep copy.
 */
public class DeepCloner {

    private final Map<DeepCloneable, DeepCloneable> map = Maps.newHashMap();
    private DeepCloneable keyForExpectedPut = null;

    private DeepCloner() {
    }

    private boolean insertIsRequired() {
        return keyForExpectedPut != null;
    }

    /**
     * Add the given {@code clone} as the clone of the {@code DeepCloneable} of the last
     * {@link #clone(DeepCloneable, Class)} or {@link #cloneField(DeepCloneable, Class)} operation.
     * This method must be called by the clone before the clone itself clones any of it's {@code DeepCloneable} fields.
     * @param clone the clone to add
     */
    public void addClone(DeepCloneable clone) {
        checkNotNull(clone);
        assert keyForExpectedPut != null;
        checkArgument(clone.getClass().equals(keyForExpectedPut.getClass()),
                "Class of clone was expected to be " + keyForExpectedPut.getClass() +  ", but is of type " + clone.getClass());
        map.put(keyForExpectedPut, clone);
        keyForExpectedPut = null;
    }

    /**
     * Clone a {@code DeepCloneable} object and return the clone casted to {@code T}
     * @param cloneable the object to clone
     * @param clazz the class for type {@code T}
     * @param <T> the type of the {@code DeepCloneable} object and it's clone
     * @return a clone of {@code cloneable}
     */
    public <T extends DeepCloneable> T cloneField(@Nullable T cloneable, Class<T> clazz) {
        checkNotNull(clazz);
        if (insertIsRequired())
            throw new IllegalStateException(
                    "A clone should createChildNode an entry for itself first with cloner.setAsCloned(cloneable, this)");

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

    /**
     * Clone a {@code DeepCloneable} object and return the clone casted to {@code T}
     * @param cloneable the object to clone
     * @param clazz the class for type {@code T}
     * @param <T> the type of the {@code DeepCloneable} object and it's clone
     * @return a clone of {@code cloneable}
     */
    public static <T extends DeepCloneable> T clone(@Nullable T cloneable, Class<T> clazz) {
        checkNotNull(clazz);
        return new DeepCloner().cloneField(cloneable, clazz);
    }
}
