package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import see.INode;
import see.Resolver;
import see.See;
import see.SeeException;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 19.05.11
 * Time: 09:41
 */
public class SeeEvaluator implements Evaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeeEvaluator.class);
    private static final See see = See.create();

    private @Nullable INode inode;
    private String expression = "";

    public SeeEvaluator() {
    }

    public SeeEvaluator(String expression, @Nullable VariableResolver resolver) {
        setExpression(expression);
        setResolver(resolver);
    }

    @Override
    public void setResolver(@Nullable VariableResolver resolver) {
        if (resolver != null)
            see.setParent(new SeeResolverAdaptor(resolver));
    }

    @Override
    public VariableResolver getResolver() {
        return (VariableResolver) see.getParent();
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
    public String evaluateAsString() throws EvaluationException {
        try {
            LOGGER.debug("Evaluating INode {}", inode);
            String ret = see.evalAsString(inode);
            LOGGER.debug("Result: {}", ret);
            return ret;
        }
        catch (Exception ex) {
            LOGGER.error("Evaluation failed", ex);
            throw new EvaluationException(ex);
        }
    }

    @Override
    public void setExpression(String expression) {
        this.expression = checkNotNull(expression);
        try {
            inode = see.parse(expression);
            LOGGER.debug("See parsed Expression {} to Node {}.", expression, inode);
        }
        catch (SeeException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getExpression() {
        return expression;
    }

    private static class SeeResolverAdaptor extends ForwardingVariableResolver implements Resolver {

        private final VariableResolver resolver;

        private SeeResolverAdaptor(VariableResolver resolver) {
            this.resolver = checkNotNull(resolver);
        }

        @Override
        public VariableResolver delegate() {
            return resolver;
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
            return canResolve(varName);
        }
    }
}
