package org.asoem.greyfish.core.eval;

import com.google.common.base.Function;
import org.asoem.greyfish.core.individual.GFComponent;

/**
 * User: christoph
 * Date: 14.09.11
 * Time: 09:16
 *
 *
 */
public interface ResolverConverter {
    /**
     *
     *
     * @param varName The name of a variable which is fed into a {@link GreyfishVariableResolver}
     * @param context The context of the variable definition.
     * @return a {@link Function} of the {@code context} identified by {@code varName}
     * @throws VariableResolutionException if the variable identified by varName has wrong syntax or does not match any function.
     */
    public <T extends GFComponent> Function<T, ?> get(String varName, Class<T> context) throws VariableResolutionException;
}
