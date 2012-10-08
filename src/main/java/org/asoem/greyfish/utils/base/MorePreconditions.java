package org.asoem.greyfish.utils.base;

/**
 * User: christoph
 * Date: 31.01.12
 * Time: 16:33
 */
public final class MorePreconditions {

    public static void checkMutability(Freezable freezable) {
        if (freezable.isFrozen()) throw new IllegalStateException("Component is frozen");
    }
}
