package org.asoem.greyfish.core.eval;

import com.google.inject.Inject;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 14:25
 */
public class VanillaExpressionFactory implements ExpressionFactory {

    private final EvaluatorFactory evaluatorProvider;

    @Inject
    public VanillaExpressionFactory(EvaluatorFactory evaluatorFactory) {
        this.evaluatorProvider = evaluatorFactory;
    }

    @Override
    public Expression compile(String expression) {
        return new VanillaExpression(expression, evaluatorProvider);
    }
}
