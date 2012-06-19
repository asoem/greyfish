package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.simpleframework.xml.Element;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 15.05.12
 * Time: 12:13
 */
public class GreyfishExpressionCallback<C, T> implements Callback<C, T> {

    @Element
    private final GreyfishExpression expression;

    @Element
    private final Class<T> returnType;

    public GreyfishExpressionCallback(GreyfishExpression expression, Class<T> returnType) {
        this.returnType = checkNotNull(returnType);
        this.expression = checkNotNull(expression);
    }

    @Override
    public T apply(C caller, Map<String, ?> arguments) {
        return expression.evaluateForContext(caller, arguments).as(returnType);
    }

    public static <C, T> GreyfishExpressionCallback<C, T> create(GreyfishExpression expression, Class<T> returnType) {
        return new GreyfishExpressionCallback<C, T>(expression, returnType);
    }

    public GreyfishExpression getExpression() {
        return expression;
    }

    public Class<T> getReturnType() {
        return returnType;
    }
}
