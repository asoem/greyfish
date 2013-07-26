package org.asoem.greyfish.core.eval;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 *
 */
public class GreyfishExpressionFactoryHolder {

    @Inject private static GreyfishExpressionFactory factory;

    /**
     * Create a new {@code GreyfishExpression} based on the given {@code expression}
     * and the injected {@link Provider} of {@link Evaluator} instances.
     * @param expression The expression string
     * @return A new GreyfishExpression
     */
    public static GreyfishExpression compile(final String expression) {
        return factory.compile(expression);
    }

    public static GreyfishExpressionFactory get() {
        return factory;
    }
}
