package org.asoem.greyfish.core.eval;


public interface Expression {
    EvaluationResult evaluate(VariableResolver resolver) throws EvaluationException;

    EvaluationResult evaluate() throws EvaluationException;

    String getExpression();

    Evaluator getEvaluator();
}
