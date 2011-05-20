package org.asoem.greyfish.core.eval;

import net.sourceforge.jeval.Evaluator;
import net.sourceforge.jeval.function.FunctionException;

import javax.annotation.Nonnull;

/**
 * User: christoph
 * Date: 18.05.11
 * Time: 16:57
 */
public class JEvalExpressionParser implements ExpressionParser {

    private final Evaluator evaluator;

    public JEvalExpressionParser(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public void parse(String expression) {
        try {
            evaluator.parse(expression);
        } catch (net.sourceforge.jeval.EvaluationException e) {
            throw new IllegalArgumentException("Expression is not valid.", e);
        }
    }

    @Override
    public double evaluate() throws EvaluationException {
        try {
            return Double.valueOf(evaluator.evaluate());
        } catch (net.sourceforge.jeval.EvaluationException e) {
            throw new EvaluationException("Expression could not be evaluated.", e);
        }
    }

    @Override
    public void setResolver(VariableResolver resolver) {
        evaluator.setVariableResolver(new JEvalVariableResolverAdaptor(resolver));
    }

    private static class JEvalVariableResolverAdaptor implements VariableResolver, net.sourceforge.jeval.VariableResolver {

        private final VariableResolver variableResolver;

        public JEvalVariableResolverAdaptor(VariableResolver variableResolver) {
            this.variableResolver = variableResolver;
        }

        @Override
        public Object resolve(@Nonnull String varName) {
            return variableResolver.resolve(varName);
        }

        @Override
        public String resolveVariable(String s) throws FunctionException {
            return String.valueOf(resolve(s));
        }
    }
}
