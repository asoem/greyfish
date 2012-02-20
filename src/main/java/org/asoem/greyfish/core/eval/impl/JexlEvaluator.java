package org.asoem.greyfish.core.eval.impl;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.asoem.greyfish.core.eval.*;
import org.asoem.greyfish.utils.math.RandomUtils;

/**
 * User: christoph
 * Date: 17.02.12
 * Time: 12:14
 */
public class JEXLEvaluator extends AbstractEvaluator {

    private static final JexlEngine JEXL_ENGINE = new JexlEngine();
    static {
        JEXL_ENGINE.setFunctions(ImmutableMap.<String, Object>of(
                "fish", GreyfishVariableFactory.class,
                "math", Math.class,
                "rand", RandomUtils.class));
    }
    private Expression expression;
    private JEXLResolverAdaptor resolver;

    @Override
    public EvaluationResult evaluate() {
        return new GenericEvaluationResult(expression.evaluate(resolver));
    }

    @Override
    public void setExpression(String expression) throws SyntaxException {
        this.expression = JEXL_ENGINE.createExpression(prepare(expression));
    }

    private String prepare(String expression) {
        // This will lift some function in the global namespace and allows users to write 'fun' instead of 'ns:fun'.
        return expression
                .replaceAll("\\$\\(([^\\)]+)\\)", "fish:\\$($1)")
                .replaceAll("((rnorm|rpois|runif)\\([^\\)]+\\))", "rand:$1")
                .replaceAll("((min|max|abs|sin|cos|tan|log|log10)\\([^\\)]+\\))", "math:$1");
    }

    @Override
    public void setResolver(VariableResolver resolver) {
        this.resolver = new JEXLResolverAdaptor(resolver);
    }

    private class JEXLResolverAdaptor extends ForwardingVariableResolver implements JexlContext {
        private final VariableResolver resolver;

        public JEXLResolverAdaptor(VariableResolver resolver) {
            this.resolver = resolver;
        }

        @Override
        public Object get(String s) {
                return resolve(s);
        }

        @Override
        public void set(String s, Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean has(String s) {
            return canResolve(s);
        }

        @Override
        public VariableResolver delegate() {
            return resolver;
        }
    }
}
