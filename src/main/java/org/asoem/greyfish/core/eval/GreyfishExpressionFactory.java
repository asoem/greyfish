package org.asoem.greyfish.core.eval;

import com.google.inject.Inject;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 12:08
 */
public class GreyfishExpressionFactory implements ExpressionFactory {

    private final EvaluatorFactory evaluatorFactory;

    @Inject
    public GreyfishExpressionFactory(EvaluatorFactory evaluatorFactory) {
        this.evaluatorFactory = evaluatorFactory;
    }

    @Override
    public boolean isValidExpression(String s) {
        try {
            new GreyfishExpression(s, evaluatorFactory); // TODO: Using exceptions for control flow is not good practice
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    public GreyfishExpression compile(String s) {
        return new GreyfishExpression(s, evaluatorFactory);
    }
}
