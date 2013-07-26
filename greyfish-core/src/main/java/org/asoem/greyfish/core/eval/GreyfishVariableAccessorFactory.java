package org.asoem.greyfish.core.eval;

import com.google.common.base.Function;

/**
 * User: christoph
 * Date: 14.09.11
 * Time: 09:16
 *
 *
 */
public interface GreyfishVariableAccessorFactory {

    /**
     * @param varName The name of a variable
     * @param context The context of the variable definition.
     * @return a {@link Function} of the {@code context} identified by {@code varName}
     * @throws VariableResolutionException if the variable identified by varName has wrong syntax or does not match any function.
     */
    <T> Function<T, ?> get(String varName, Class<T> context) throws VariableResolutionException;

    /**
     *
     *
     * @param name The name of a variable
     * @param contextClass The class of the context object passed to the resolved function
     * @return {@code true} if this GreyfishVariableAccessorFactory can convert name x contextClass, {@code false} otherwise
     */
    boolean canConvert(String name, Class<?> contextClass);
}
