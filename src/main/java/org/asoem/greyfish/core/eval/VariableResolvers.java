package org.asoem.greyfish.core.eval;

import net.sourceforge.jeval.VariableResolver;
import net.sourceforge.jeval.function.FunctionException;

/**
 * User: christoph
 * Date: 20.04.11
 * Time: 14:22
 */
public class VariableResolvers {
    static VariableResolver concat(final VariableResolver... resolvers) {
        return new VariableResolver() {
            @Override
            public String resolveVariable(String s) throws FunctionException {
                for (VariableResolver resolver : resolvers) {
                    String ret = resolver.resolveVariable(s);
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
            public String resolveVariable(String s) throws FunctionException {
                String ret;
                if ((ret = resolver1.resolveVariable(s)) == null)
                    ret = resolver2.resolveVariable(s);
                return ret;
            }
        };
    }
}
