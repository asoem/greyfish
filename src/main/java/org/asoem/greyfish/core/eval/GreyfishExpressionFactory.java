package org.asoem.greyfish.core.eval;

import com.google.inject.Inject;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 12:08
 */
public class GreyfishExpressionFactory implements ExpressionFactory {

    private final EvaluatorFactory evaluatorProvider;

    @Inject
    public GreyfishExpressionFactory(EvaluatorFactory evaluatorProvider) {
        this.evaluatorProvider = evaluatorProvider;
    }

    @Override
    public GreyfishExpression compile(String expression) {
        return new GreyfishExpression(expression, evaluatorProvider);
    }
}
