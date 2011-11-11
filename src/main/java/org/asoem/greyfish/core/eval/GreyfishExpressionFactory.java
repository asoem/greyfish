package org.asoem.greyfish.core.eval;

/**
 * User: christoph
 * Date: 03.11.11
 * Time: 09:31
 */
public interface GreyfishExpressionFactory {
    /**
     *
     *
     * @param expression The expression string
     * @return a new {@code GreyfishExpression}
     * @throws SyntaxException if the {@code expression} cannot be evaluated by the {@link Evaluator} this factory has to provide
     */
    GreyfishExpression create(String expression) throws SyntaxException;
}
