package org.asoem.greyfish.core.eval.impl;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import javolution.lang.MathLib;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.asoem.greyfish.core.eval.*;
import org.asoem.greyfish.utils.math.RandomUtils;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 17.02.12
 * Time: 12:14
 */
public class CommonsJEXLEvaluator implements Evaluator, Serializable {

    private static final JexlEngine JEXL_ENGINE = new JexlEngine();
    static {
        JEXL_ENGINE.setFunctions(ImmutableMap.<String, Object>of(
                "fish", GreyfishVariableFactory.class,
                "math", Math.class,
                "rand", RandomUtils.class));
    }
    private static final ImmutableMap<String, Object> GLOBAL_VARIABLES = ImmutableMap.<String, Object>builder()
            .put("PI", MathLib.PI)
            .put("HALF_PI", MathLib.HALF_PI)
            .put("PI_SQUARE", MathLib.PI_SQUARE)
            .put("TWO_PI", MathLib.TWO_PI)
            .put("FOUR_PI", MathLib.FOUR_PI)
            .put("E", MathLib.E)
            .build();

    private final Expression expression;

    public CommonsJEXLEvaluator(String expression) {
        checkNotNull(expression);
        this.expression = JEXL_ENGINE.createExpression(prepare(expression));
        assert expression != null;
    }

    @Override
    public EvaluationResult evaluate(VariableResolver resolver) {
        checkNotNull(resolver);
        final Object result = expression.evaluate(addGlobalVariables(resolver));
        return new ConvertingEvaluationResult(result);
    }

    @Override
    public String getExpression() {
        return expression.getExpression();
    }

    private String prepare(String expression) {
        // This will lift some function in the global namespace and allows users to write 'fun' instead of 'ns:fun'.
        return expression
                .replaceAll("\\$\\(([^\\)]+)\\)", "fish:\\$($1)")
                .replaceAll("((rnorm|rpois|runif)\\([^\\)]+\\))", "rand:$1")
                .replaceAll("((min|max|abs|sin|cos|tan|log|log10)\\([^\\)]+\\))", "math:$1");
    }

    private JEXLResolverAdaptor addGlobalVariables(VariableResolver resolver) {
        assert resolver != null;

        final VariableResolver variableResolver = VariableResolvers.forMap(GLOBAL_VARIABLES);
        variableResolver.append(resolver);

        return new JEXLResolverAdaptor(variableResolver);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(CommonsJEXLEvaluator.class)
                .addValue(expression)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommonsJEXLEvaluator that = (CommonsJEXLEvaluator) o;

        if (!expression.getExpression().equals(that.expression.getExpression())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return expression.getExpression().hashCode();
    }

    private static class SerializedForm implements Serializable {
        final String expression;

        SerializedForm(String expression) {
            this.expression = expression;
        }
        Object readResolve() {
            return new CommonsJEXLEvaluator(expression);
        }
        private static final long serialVersionUID = 0;
    }

    Object writeReplace() {
        return new SerializedForm(expression == null ? null : expression.getExpression());
    }

    private static class JEXLResolverAdaptor extends ForwardingVariableResolver implements JexlContext, Serializable {
        private final VariableResolver resolver;

        public JEXLResolverAdaptor(VariableResolver resolver) {
            this.resolver = resolver;
        }

        @Override
        protected VariableResolver delegate() {
            return resolver;
        }

        @Override
        public Object get(String s) {
            return resolve(s);
        }

        @Override
        public void set(String s, Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean has(String s) {
            return canResolve(s);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            JEXLResolverAdaptor that = (JEXLResolverAdaptor) o;

            if (!resolver.equals(that.resolver)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return resolver.hashCode();
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this).addValue(resolver).toString();
        }

        private static final long serialVersionUID = 0;
    }

    private static final long serialVersionUID = 0;
}
