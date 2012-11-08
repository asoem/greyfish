package org.asoem.greyfish.utils.base;

/**
 * User: christoph
 * Date: 15.05.12
 * Time: 11:39
 */
public interface Callback<C, R> {
    /**
     *
     * @param caller    the object calling the callback
     * @param arguments a {@code Map} of names to values which serve as arguments to the callback
     * @return the value computed by this callback
     */
    R apply(C caller, Arguments arguments);
}
