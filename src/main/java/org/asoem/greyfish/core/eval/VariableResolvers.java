package org.asoem.greyfish.core.eval;

import java.util.Map;

/**
 * User: christoph
 * Date: 20.04.11
 * Time: 14:22
 */
public class VariableResolvers {
    public static VariableResolver forMap(final Map<? extends String, ?> map) {

        return new AbstractVariableResolver() {
            @Override
            public boolean canResolveLocal(String name) {
                return map.containsKey(name);
            }

            @Override
            public Object resolveLocal(String varName) throws VariableResolutionException {
                return map.get(varName);
            }
        };
    }
}
