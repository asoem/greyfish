package org.asoem.greyfish.core.eval;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 12:04
 */
public interface ExpressionFactory {
    
    Expression compile(String expression);
}
