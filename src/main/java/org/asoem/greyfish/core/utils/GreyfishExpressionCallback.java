package org.asoem.greyfish.core.utils;

import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.utils.base.Arguments;
import org.asoem.greyfish.utils.base.Callback;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 15.05.12
 * Time: 12:13
 */
public class GreyfishExpressionCallback<C, T> implements Callback<C, T>, Serializable {

    private final GreyfishExpression expression;
    private final Class<? super T> returnType;

    public GreyfishExpressionCallback(GreyfishExpression expression, Class<? super T> returnType) {
        this.returnType = checkNotNull(returnType);
        this.expression = checkNotNull(expression);
    }

    public GreyfishExpressionCallback(GreyfishExpression expression, TypeToken<T> returnType) {
        this.returnType = checkNotNull(returnType.getRawType());
        this.expression = checkNotNull(expression);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T apply(C caller, Arguments arguments) {
        return (T) expression.evaluateForContext(caller, arguments).as(returnType);
    }

    public static <C, T> GreyfishExpressionCallback<C, T> create(GreyfishExpression expression, Class<T> returnType) {
        return new GreyfishExpressionCallback<C, T>(expression, returnType);
    }

    public static <C, T> GreyfishExpressionCallback<C, T> create(GreyfishExpression expression, TypeToken<T> returnType) {
        return new GreyfishExpressionCallback<C, T>(expression, returnType);
    }

    public GreyfishExpression getExpression() {
        return expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GreyfishExpressionCallback)) return false;

        GreyfishExpressionCallback that = (GreyfishExpressionCallback) o;

        if (!expression.equals(that.expression)) return false;
        if (!returnType.equals(that.returnType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = expression.hashCode();
        result = 31 * result + returnType.hashCode();
        return result;
    }

    private static final long serialVersionUID = 0;
}
