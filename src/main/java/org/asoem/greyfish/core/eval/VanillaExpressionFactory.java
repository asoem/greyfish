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
    public boolean isValidExpression(String s) {
        try {
            new VanillaExpression(s, evaluatorProvider);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Expression compile(String s) {
        return new VanillaExpression(s, evaluatorProvider);
    }
}
