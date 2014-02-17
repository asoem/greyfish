package org.asoem.greyfish.core.eval;

/**
 * An evaluator is an expression language evaluation engine.
 */
public interface Evaluator {
    /**
     * Tells this evaluator to evaluate the expression associated with it.
     *
     * @param resolver the variable resolver to use
     * @return the result of the evaluation
     * @throws EvaluationException if the evaluation failed
     * @see org.asoem.greyfish.core.eval.Expression
     */
    EvaluationResult evaluate(VariableResolver resolver);

    /**
     * The expression associated with this evaluator.
     *
     * @return the expression string
     */
    String getExpression();
}
