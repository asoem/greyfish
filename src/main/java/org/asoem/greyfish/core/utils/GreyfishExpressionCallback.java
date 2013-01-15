package org.asoem.greyfish.core.utils;

import com.google.common.base.Function;
import org.asoem.greyfish.core.eval.EvaluationResult;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.utils.base.Arguments;
import org.asoem.greyfish.utils.base.Callback;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.text.ParseException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 15.05.12
 * Time: 12:13
 */
public class GreyfishExpressionCallback<C, T> implements Callback<C, T>, Serializable {

    private final GreyfishExpression expression;
    private final Function<EvaluationResult, T> conversionFunction;

    public GreyfishExpressionCallback(GreyfishExpression expression, Function<EvaluationResult, T> conversionFunction) {
        this.conversionFunction = checkNotNull(conversionFunction);
        this.expression = checkNotNull(expression);
    }

    @Override
    public T apply(C caller, Arguments arguments) {
        return conversionFunction.apply(expression.evaluateForContext(caller, arguments));
    }

    public GreyfishExpression getExpression() {
        return expression;
    }

    public static <C, T> GreyfishExpressionCallback<C, T> create(GreyfishExpression expression, Function<EvaluationResult, T> conversionFunction) {
        return new GreyfishExpressionCallback<C, T>(expression, conversionFunction);
    }

    public static <C, T> GreyfishExpressionCallback<C, T> create(GreyfishExpression expression, final Class<T> clazz) {
        return new GreyfishExpressionCallback<C, T>(expression, new CastingConversion<T>(clazz));
    }

    public static <C> GreyfishExpressionCallback<C, Double> doubleExpression(GreyfishExpression expression) {
        return new GreyfishExpressionCallback<C, Double>(expression, DoubleParsingFunction.INSTANCE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GreyfishExpressionCallback that = (GreyfishExpressionCallback) o;

        if (!conversionFunction.equals(that.conversionFunction)) return false;
        if (!expression.equals(that.expression)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = expression.hashCode();
        result = 31 * result + conversionFunction.hashCode();
        return result;
    }

    private static final long serialVersionUID = 0;

    private static class CastingConversion<T> implements Function<EvaluationResult, T>, Serializable {
        private final Class<T> clazz;

        private CastingConversion(Class<T> clazz) {
            this.clazz = checkNotNull(clazz);
        }

        @Nullable
        @Override
        public T apply(EvaluationResult evaluationResult) {
            return clazz.cast(evaluationResult.get());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CastingConversion that = (CastingConversion) o;

            if (!clazz.equals(that.clazz)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return clazz.hashCode();
        }

        private static final long serialVersionUID = 0;
    }

    private static enum DoubleParsingFunction implements Function<EvaluationResult, Double> {
        INSTANCE;

        @Nullable
        @Override
        public Double apply(EvaluationResult evaluationResult) {
            try {
                return evaluationResult.asDouble();
            } catch (ParseException e) {
                throw new AssertionError(e);
            }
        }
    }
}
