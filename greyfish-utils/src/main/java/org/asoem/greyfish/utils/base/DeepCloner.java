package org.asoem.greyfish.utils.base;

import javax.annotation.Nullable;

/**
 * A helper to deeply clone {@code DeepCloneable} objects.
 */
public interface DeepCloner {

    /**
     * Register {@code clone} as the clone of {@code original}.
     * This method must be called in the (super)constructor of {@code clone}
     * before any of the fields get cloned using this cloner.
     *
     * @param original a cloneable
     * @param clone the clone of original
     */
    <T extends DeepCloneable> void addClone(T original, T clone);

    /**
     * Get the deep clone of {@code cloneable}
     *
     * @param cloneable the object to clone
     * @return the deep clone of {@code cloneable}, {@code null} if {@code cloneable} is {@code null}
     */
    @Nullable
    <T extends DeepCloneable> T getClone(@Nullable T cloneable);

}
