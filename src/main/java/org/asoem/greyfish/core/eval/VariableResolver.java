package org.asoem.greyfish.core.eval;

import javax.annotation.Nullable;
import javax.script.Bindings;

/**
 * User: christoph
 * Date: 16.09.11
 * Time: 11:09
 */
public interface VariableResolver {

    Bindings bindings();

    /**
     * Check if the variable {@code name} van be resolved by this {@code VariableResolver}
     * @param name the name of the variable to check
     * @return {@code true} if the variable can be resolved, {@code false} otherwise.
     */
    boolean canResolve(String name);

    /**
     * Resolve the value for the variable {@code varName}
     * @param varName the name of the variable
     * @return The value for {@code varName}
     * @throws VariableResolutionException if {@code varName} can not be resolved for any reason.
     */
    @Nullable Object resolve(String varName) throws VariableResolutionException;

    /**
     * Get the {@code VariableResolver} which is connected to this {@code VariableResolver},
     * which will be used to resolve variables if this one cannot resolve a queried variable
     * @return the next {@code VariableResolver} in the resolver chain
     */
    @Nullable
    VariableResolver getNext();

    /**
     * Add a {@code VariableResolver} to this resolver to build a chain of resolvers which will be queried for a variable.
     * @param next the resolver to append to this resolver
     */
    void setNext(@Nullable VariableResolver next);

}
