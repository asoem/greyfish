package org.asoem.greyfish.core.eval;

/**
 * An Expression represents a code fragment in some expression language which can be evaluated using an {@link
 * org.asoem.greyfish.core.eval.Evaluator evaluator}.
 */
public interface Expression {
    /**
     * Evaluate the expression using the given {@code resolver}.
     *
     * @param resolver the variable resolver to use.
     * @return the evaluation result
     * @throws EvaluationException if the evaluation failed
     */
    EvaluationResult evaluate(VariableResolver resolver);

    /**
     * Evaluate the expression.
     *
     * @return the evaluation result
     * @throws EvaluationException if the evaluation failed
     */
    EvaluationResult evaluate();

    /**
     * Get the expression string for this expression.
     *
     * @return the expression string
     */
    String getExpression();

    /**
     * Get the evaluator.
     *
     * @return the evaluator for this expression.
     */
    Evaluator getEvaluator();
}
