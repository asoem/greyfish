package org.asoem.greyfish.core.eval;


public interface Evaluator {
    EvaluationResult evaluate(VariableResolver resolver) throws EvaluationException;

    String getExpression();
}
