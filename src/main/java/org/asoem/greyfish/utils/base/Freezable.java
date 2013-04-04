package org.asoem.greyfish.utils.base;

/**
 * This interface is intended to assist objects that are able to switch into an immutable state.
 * The implementation is asked to intercept evaluates write accessors of this object and prevent any modification from outside.
 * It also shouldn't modify itself from inside.
 */
public interface Freezable {
    /**
     * Freeze this object. If an object is frozen, it should act as if evaluates of its fields are immutable.
     */
    void freeze();

    /**
     * Ask the object if it has been frozen.
     * @return {@code true} if the object implementing this interface is frozen (has been {@link #freeze}d), {@code false} otherwise.
     */
    boolean isFrozen();
}
