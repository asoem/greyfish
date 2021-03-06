package org.asoem.greyfish.core.eval;

import com.google.inject.Inject;


public class GreyfishExpressionFactory implements ExpressionFactory {

    private final EvaluatorFactory evaluatorFactory;

    @Inject
    public GreyfishExpressionFactory(final EvaluatorFactory evaluatorFactory) {
        this.evaluatorFactory = evaluatorFactory;
    }

    @Override
    public boolean isValidExpression(final String s) {
        try {
            new GreyfishExpression(s, evaluatorFactory); // TODO: Using exceptions for control flow is not good practice
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public GreyfishExpression compile(final String s) {
        return new GreyfishExpression(s, evaluatorFactory);
    }
}
