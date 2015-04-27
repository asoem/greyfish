/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
