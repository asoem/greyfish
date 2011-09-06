package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import see.INode;
import see.Resolver;
import see.See;
import see.SeeException;

import javax.annotation.Nonnull;

/**
 * User: christoph
 * Date: 19.05.11
 * Time: 09:41
 */
public class SeeExpressionParser implements ExpressionParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeeExpressionParser.class);
    private final static See see = See.create();
    private INode inode;

    @Override
    public void parse(String expression) throws IllegalArgumentException {
        try {
            inode = see.parse(expression);
            LOGGER.debug("See parsed Expression {} to Node {}.", expression, inode);
        }
        catch (SeeException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public double evaluateAsDouble() throws EvaluationException {
        synchronized(see) {
            try {

                LOGGER.debug("Evaluating INode {}", inode);
                double ret = see.evalAsDouble(inode);
                LOGGER.debug("Result: {}", ret);
                return ret;
            }
            catch (Exception ex) {
                LOGGER.error("Evaluation failed", ex);
                throw new EvaluationException(ex);
            }
        }
    }

    @Override
    public boolean evaluateAsBoolean() throws EvaluationException {
        synchronized(see) {
            try {
                LOGGER.debug("Evaluating INode {}", inode);
                boolean ret = see.evalAsBoolean(inode);
                LOGGER.debug("Result: {}", ret);
                return ret;
            }
            catch (Exception ex) {
                LOGGER.error("Evaluation failed", ex);
                throw new EvaluationException(ex);
            }
        }
    }

    @Override
    public void setResolver(VariableResolver resolver) {
        see.setParent(new SeeResolverAdaptor(resolver));
    }

    private static class SeeResolverAdaptor implements Resolver, VariableResolver {

        private final VariableResolver resolver;

        private SeeResolverAdaptor(VariableResolver resolver) {
            this.resolver = resolver;
        }

        @Override
        public Object resolve(@Nonnull String varName) {
            return resolver.resolve(varName);
        }

        @Override
        public Object get(String varName) {
            return resolve(varName);
        }

        @Override
        public void set(String varName, Object varValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(String varName) {
            return get(varName) != null;
        }
    }
}
