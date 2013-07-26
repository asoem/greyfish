package org.asoem.greyfish.core.eval;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 11:57
 */
public interface Expression {
    EvaluationResult evaluate(VariableResolver resolver) throws EvaluationException;

    EvaluationResult evaluate() throws EvaluationException;

    String getExpression();

    Evaluator getEvaluator();
}
