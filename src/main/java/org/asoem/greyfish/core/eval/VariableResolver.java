package org.asoem.greyfish.core.eval;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 16.09.11
 * Time: 11:09
 */
public interface VariableResolver {
    @Nullable Object resolve(String varName) throws VariableResolutionException;
}
