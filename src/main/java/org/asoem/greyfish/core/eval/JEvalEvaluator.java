package org.asoem.greyfish.core.eval;

import net.sourceforge.jeval.function.FunctionException;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 18.05.11
 * Time: 16:57
 */
public class JEvalEvaluator implements Evaluator {

    private final net.sourceforge.jeval.Evaluator evaluator = new net.sourceforge.jeval.Evaluator();

    private String expression;

    public JEvalEvaluator(String expression, VariableResolver resolver) {
        setExpression(expression);
        setResolver(resolver);
    }

    @Override
    public double evaluateAsDouble() throws EvaluationException {
        try {
            return Double.valueOf(evaluator.evaluate());
        } catch (net.sourceforge.jeval.EvaluationException e) {
            throw new EvaluationException("Expression could not be evaluated.", e);
        }
    }

    @Override
    public boolean evaluateAsBoolean() throws EvaluationException {
        try {
            return Boolean.valueOf(evaluator.evaluate());
        } catch (net.sourceforge.jeval.EvaluationException e) {
            throw new EvaluationException("Expression could not be evaluated.", e);
        }
    }

    @Override
    public void setExpression(String expression) {
        this.expression = checkNotNull(expression);
        try {
            evaluator.parse(expression);
        } catch (net.sourceforge.jeval.EvaluationException e) {
            throw new IllegalArgumentException("Expression is not valid.", e);
        }
    }

    @Override
    public String getExpression() {
        return expression;
    }

    @Override
    public void setResolver(VariableResolver resolver) {
        evaluator.setVariableResolver(new JEvalVariableResolverAdaptor(resolver));
    }

    private static class JEvalVariableResolverAdaptor extends ForwardingVariableResolver implements net.sourceforge.jeval.VariableResolver {

        private final VariableResolver variableResolver;

        public JEvalVariableResolverAdaptor(VariableResolver variableResolver) {
            this.variableResolver = checkNotNull(variableResolver);
        }

        @Override
        public VariableResolver delegate() {
            return variableResolver;
        }

        @Override
        public String resolveVariable(String s) throws FunctionException {
            return String.valueOf(resolve(s));
        }
    }
}
