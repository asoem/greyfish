package org.asoem.greyfish.utils.base;

import java.util.Map;

/**
 * User: christoph
 * Date: 15.05.12
 * Time: 11:39
 */
public interface Callback<C, R> {
    /**
     *
     * @param caller    the object calling the callback
     * @param args      a map of named arguments
     * @return the value computed by this callback
     */
    R apply(C caller, Map<String, ?> args);
}
