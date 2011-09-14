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
     * @param varName The name of a variable which is fed into a {@link org.asoem.greyfish.core.eval.VariableResolver}
     * @param context The context of the variable definition.
     * @return a {@link Function} of the {@code context} identified by {@code varName}
     */
    public Function<GFComponent, Object> get(String varName, Class<? extends GFComponent> context);
}
