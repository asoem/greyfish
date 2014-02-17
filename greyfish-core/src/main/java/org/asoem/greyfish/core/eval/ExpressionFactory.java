package org.asoem.greyfish.core.eval;


public interface ExpressionFactory {
    boolean isValidExpression(String s);

    Expression compile(String s);
}
