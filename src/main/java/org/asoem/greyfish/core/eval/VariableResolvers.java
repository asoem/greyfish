package org.asoem.greyfish.core.eval;

/**
 * User: christoph
 * Date: 20.04.11
 * Time: 14:22
 */
public class VariableResolvers {
    static VariableResolver concat(final VariableResolver... resolvers) {
        return new VariableResolver() {
            @Override
            public String resolve(String s) {
                for (VariableResolver resolver : resolvers) {
                    String ret = resolver.resolve(s);
                    if (ret != null)
                        return ret;
                }
                return null;
            }
        };
    }

    static VariableResolver concat(final VariableResolver resolver1, final VariableResolver resolver2) {
        return new VariableResolver() {
            @Override
            public String resolve(String s) {
                String ret = resolver1.resolve(s);
                if (ret == null)
                    ret = resolver2.resolve(s);
                return ret;
            }
        };
    }
}
