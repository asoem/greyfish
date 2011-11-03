package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.AgentComponent;

/**
 * User: christoph
 * Date: 03.11.11
 * Time: 09:31
 */
public interface GreyfishExpressionFactory {
    /**
     *
     * @param expression The expression string
     * @param contextClass The {@code Class} of the context object supplied to to {@code GreyfishExpression}
     * @param <T> the type of the context object
     * @return a new {@code GreyfishExpression}
     * @throws SyntaxException if the {@code expression} cannot be evaluated by the {@link Evaluator} this factory has to provide
     */
    <T extends AgentComponent> GreyfishExpression<T> create(String expression, Class<T> contextClass) throws SyntaxException;
}
