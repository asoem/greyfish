package org.asoem.greyfish.core.utils;

import com.google.common.base.Function;
import org.asoem.greyfish.core.eval.EvaluationResult;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.utils.base.Callback;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;


public class GreyfishExpressionCallback<C, T> implements Callback<C, T>, Serializable {

    private final GreyfishExpression expression;
    private final Function<EvaluationResult, T> conversionFunction;

    public GreyfishExpressionCallback(final GreyfishExpression expression, final Function<EvaluationResult, T> conversionFunction) {
        this.conversionFunction = checkNotNull(conversionFunction);
        this.expression = checkNotNull(expression);
    }

    @Override
    public T apply(final C caller, final Map<String, ?> args) {
        return conversionFunction.apply(expression.evaluateForContext(caller, args));
    }

    public GreyfishExpression getExpression() {
        return expression;
    }

    public static <C, T> GreyfishExpressionCallback<C, T> create(final GreyfishExpression expression, final Function<EvaluationResult, T> conversionFunction) {
        return new GreyfishExpressionCallback<C, T>(expression, conversionFunction);
    }

    public static <C, T> GreyfishExpressionCallback<C, T> create(final GreyfishExpression expression, final Class<T> clazz) {
        return new GreyfishExpressionCallback<C, T>(expression, new CastingConversion<T>(clazz));
    }

    public static <C> GreyfishExpressionCallback<C, Double> doubleExpression(final GreyfishExpression expression) {
        return new GreyfishExpressionCallback<C, Double>(expression, DoubleParsingFunction.INSTANCE);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final GreyfishExpressionCallback that = (GreyfishExpressionCallback) o;

        if (!conversionFunction.equals(that.conversionFunction)) {
            return false;
        }
        if (!expression.equals(that.expression)) {
            return false;
        }

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

        private CastingConversion(final Class<T> clazz) {
            this.clazz = checkNotNull(clazz);
        }

        @Nullable
        @Override
        public T apply(final EvaluationResult evaluationResult) {
            return clazz.cast(evaluationResult.get());
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final CastingConversion that = (CastingConversion) o;

            if (!clazz.equals(that.clazz)) {
                return false;
            }

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
        public Double apply(final EvaluationResult evaluationResult) {
            return evaluationResult.asDouble();
        }
    }
}
