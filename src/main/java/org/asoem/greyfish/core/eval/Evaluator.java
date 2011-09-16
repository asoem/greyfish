package org.asoem.greyfish.core.eval;


/**
 * User: christoph
 * Date: 18.05.11
 * Time: 16:56
 */
public interface Evaluator {

    double evaluateAsDouble() throws EvaluationException;
    boolean evaluateAsBoolean() throws EvaluationException;

    void setExpression(String expression) throws SyntaxException;
    String getExpression();
}
