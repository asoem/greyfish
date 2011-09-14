package org.asoem.greyfish.core.eval;

import javax.annotation.Nonnull;

/**
 * User: christoph
 * Date: 20.04.11
 * Time: 14:22
 */
public class VariableResolvers {
    static VariableResolver concat(final VariableResolver... resolvers) {
        return new AbstractVariableResolver() {
            @Override
            public Object resolve(@Nonnull  String s) {
                for (VariableResolver resolver : resolvers) {
                    Object ret = resolver.resolve(s);
                    if (ret != null)
                        return ret;
                }
                return null;
            }
        };
    }

    static VariableResolver concat(final VariableResolver resolver1, final VariableResolver resolver2) {
        return new AbstractVariableResolver() {
            @Override
            public Object resolve(@Nonnull String s) {
                Object ret = resolver1.resolve(s);
                if (ret == null)
                    ret = resolver2.resolve(s);
                return ret;
            }
        };
    }
}
