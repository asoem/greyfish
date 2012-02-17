package org.asoem.greyfish.core.eval;


/**
 * User: christoph
 * Date: 18.05.11
 * Time: 16:56
 */
public interface Evaluator {

    EvaluationResult evaluate() throws EvaluationException;
    
    void setExpression(String expression) throws SyntaxException;

    void setResolver(VariableResolver resolver);
}
