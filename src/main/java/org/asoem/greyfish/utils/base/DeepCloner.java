package org.asoem.greyfish.utils.base;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
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
 * Here, the object should pass evaluates fields to the {@link #getClone(DeepCloneable, Class)} method, which are required for a deep copy.
 */
public class DeepCloner {

    private final Map<DeepCloneable, DeepCloneable> map = Maps.newIdentityHashMap();
    private DeepCloneable keyForExpectedPut = null;

    private DeepCloner() {
    }

    private boolean insertIsRequired() {
        return keyForExpectedPut != null;
    }

    /**
     * Add the given {@code clone} as the clone of the {@code DeepCloneable} of the last
     * {@link #clone(DeepCloneable, Class)} or {@link #getClone(DeepCloneable, Class)} operation.
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
     * Get the deep clone of {@code cloneable} casted to {@code clazz}.
     *
     * @param cloneable the object to clone
     * @param clazz the class of {@code cloneable}
     * @return the deep clone of {@code cloneable}, {@code null} if {@code cloneable} is {@code null}
     */
    @Nullable
    public <T extends DeepCloneable> T getClone(@Nullable T cloneable, Class<T> clazz) {
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
            final DeepCloneable clone = cloneable.deepClone(this);
            checkNotNull(clone, "Deep clone of a non-null cloneable must not be null: {}", cloneable);
            return clazz.cast(clone);
        }
    }

    /**
     * Get the clones for given {@code cloneableElements}
     * @param cloneableElements the objects to get it's deep clone for
     * @param elementClazz the class of the cloneable objects
     * @param <T> the type of the cloneable objects
     * @return an {@code Iterable} of the deep clones of given {@code cloneableElements}
     */
    public <T extends DeepCloneable> Iterable<T> getClones(Iterable<T> cloneableElements, final Class<T> elementClazz) {
          return Iterables.transform(cloneableElements, new Function<T, T>() {
              @Override
              public T apply(@Nullable T input) {
                  return getClone(input, elementClazz);
              }
          });
    }

    /**
     * Creates a deep clone of {@code cloneable}
     * @param cloneable the object to clone deeply
     * @param clazz the class of {@code cloneable}
     * @param <T> the type of {@code cloneable}
     * @return a deep clone of {@code cloneable}
     */
    public static <T extends DeepCloneable> T clone(@Nullable T cloneable, Class<T> clazz) {
        checkNotNull(clazz);
        return new DeepCloner().getClone(cloneable, clazz);
    }
}
