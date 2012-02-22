package org.asoem.greyfish.core.eval;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 14:25
 */
public class VanillaExpressionFactory implements ExpressionFactory {

    private final Provider<Evaluator> evaluatorProvider;

    @Inject
    public VanillaExpressionFactory(Provider<Evaluator> evaluatorProvider) {
        this.evaluatorProvider = evaluatorProvider;
    }

    @Override
    public Expression compile(String expression) {
        return new VanillaExpression(expression, evaluatorProvider.get());
    }
}
