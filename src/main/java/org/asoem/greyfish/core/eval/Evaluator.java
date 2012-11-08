package org.asoem.greyfish.core.eval;


/**
 * User: christoph
 * Date: 18.05.11
 * Time: 16:56
 */
public interface Evaluator {

    EvaluationResult evaluate(VariableResolver resolver) throws EvaluationException;

    String getExpression();
}
