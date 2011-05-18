package org.asoem.greyfish.core.eval;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import net.sourceforge.jeval.function.FunctionException;

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
        } catch (EvaluationException e) {
            throw new IllegalArgumentException("Expression is not valid.", e);
        }
    }

    @Override
    public double evaluate() {
        try {
            return Double.valueOf(evaluator.evaluate());
        } catch (EvaluationException e) {
            throw new IllegalStateException("Expression could not be evaluated.", e);
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
        public String resolve(String varName) {
            return variableResolver.resolve(varName);
        }

        @Override
        public String resolveVariable(String s) throws FunctionException {
            return resolve(s);
        }
    }
}
