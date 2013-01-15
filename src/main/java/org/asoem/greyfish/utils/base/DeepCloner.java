package org.asoem.greyfish.utils.base;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 07.01.13
 * Time: 14:59
 */
public interface DeepCloner {
    <T extends DeepCloneable> void addClone(T original, T clone);

    @Nullable
    <T extends DeepCloneable> T getClone(@Nullable T cloneable);

}
