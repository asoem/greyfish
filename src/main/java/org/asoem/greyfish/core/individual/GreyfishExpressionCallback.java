package org.asoem.greyfish.core.individual;

import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.io.persistence.TypeTokenConverter;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.convert.Convert;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 15.05.12
 * Time: 12:13
 */
public class GreyfishExpressionCallback<C, T> implements Callback<C, T> {

    @Element(name = "expression")
    private final GreyfishExpression expression;

    @Element(name = "returnType")
    @Convert(TypeTokenConverter.class)
    private final TypeToken<T> returnType;

    public GreyfishExpressionCallback(GreyfishExpression expression, Class<T> returnType) {
        this.returnType = checkNotNull(TypeToken.of(returnType));
        this.expression = checkNotNull(expression);
    }

    public GreyfishExpressionCallback(@Element(name = "expression") GreyfishExpression expression,
                                      @Element(name = "returnType") TypeToken<T> returnType) {
        this.returnType = checkNotNull(returnType);
        this.expression = checkNotNull(expression);
    }

    @Override
    public T apply(C caller, Map<String, ?> arguments) {
        return (T) expression.evaluateForContext(caller, arguments).as(returnType.getRawType());
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

    public TypeToken<T> getReturnType() {
        return returnType;
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
}
