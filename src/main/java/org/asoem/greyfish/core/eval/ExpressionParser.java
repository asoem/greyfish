package org.asoem.greyfish.core.eval;


/**
 * User: christoph
 * Date: 18.05.11
 * Time: 16:56
 */
public interface ExpressionParser {
    void parse(String expression);
    double evaluate();
    void setResolver(VariableResolver resolver);
}
