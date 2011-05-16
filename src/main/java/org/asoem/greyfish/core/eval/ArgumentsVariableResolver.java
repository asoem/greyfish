package org.asoem.greyfish.core.eval;

import net.sourceforge.jeval.VariableResolver;
import net.sourceforge.jeval.function.FunctionException;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;

import java.util.regex.Pattern;

/**
* User: christoph
* Date: 20.04.11
* Time: 14:37
*/
class ArgumentsVariableResolver implements VariableResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentsVariableResolver.class);
    private final Object[] args;

    private final static Pattern pattern = Pattern.compile("x\\d+");

    public ArgumentsVariableResolver(Object[] args) {
        this.args = args;
    }

    @Override
    public String resolveVariable(String s) throws FunctionException {
        if (pattern.matcher(s).matches()) {
            int i = Integer.decode(s.substring(1));
            if (i < args.length)
                return String.valueOf(args[i]);
            else
                LOGGER.error("Variable {} is out of bounds for args of length {}.", s, args.length);
        }
        return null;
    }
}
