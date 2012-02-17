package org.asoem.greyfish.core.eval.impl;

import net.sourceforge.jeval.Evaluator;
import net.sourceforge.jeval.function.FunctionException;
import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.EvaluationResult;
import org.asoem.greyfish.core.eval.ForwardingVariableResolver;
import org.asoem.greyfish.core.eval.VariableResolver;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 18.05.11
 * Time: 16:57
 */
public class JEvalEvaluator extends AbstractEvaluator {

    private final Evaluator evaluator = new Evaluator();

    public JEvalEvaluator(String expression, VariableResolver resolver) {
        setExpression(expression);
        setResolver(resolver);
    }

    @Override
    public void setExpression(String expression) {
        try {
            evaluator.parse(expression);
        } catch (net.sourceforge.jeval.EvaluationException e) {
            throw new IllegalArgumentException("Expression is not valid.", e);
        }
    }

    @Override
    public void setResolver(VariableResolver resolver) {
        evaluator.setVariableResolver(new JEvalVariableResolverAdaptor(resolver));
    }

    @Override
    public EvaluationResult evaluate() throws EvaluationException {
        try {
            return new GenericEvaluationResult(evaluator.evaluate());
        } catch (net.sourceforge.jeval.EvaluationException e) {
            throw new EvaluationException("Expression could not be evaluated.", e);
        }
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
