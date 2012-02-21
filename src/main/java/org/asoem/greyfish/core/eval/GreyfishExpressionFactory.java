package org.asoem.greyfish.core.eval;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 *
 */
public class GreyfishExpressionFactory {

    @Inject private static Provider<Evaluator> evaluatorProvider;

    /**
     * Create a new {@code GreyfishExpression} based on the given {@code expression}
     * and the injected {@link Provider} of {@link Evaluator} instances.
     * @param expression The expression string
     * @return A new GreyfishExpression
     */
    public static GreyfishExpression compile(String expression) {
        assert evaluatorProvider != null;
        return new GreyfishExpression(expression, evaluatorProvider.get());
    }
}
