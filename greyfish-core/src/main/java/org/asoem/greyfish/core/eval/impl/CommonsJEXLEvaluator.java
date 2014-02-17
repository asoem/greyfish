package org.asoem.greyfish.core.eval.impl;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.math3.util.FastMath;
import org.asoem.greyfish.core.eval.*;
import org.asoem.greyfish.utils.math.RandomGenerators;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;


public class CommonsJEXLEvaluator implements Evaluator, Serializable {

    private static final JexlEngine JEXL_ENGINE = new JexlEngine();

    static {
        JEXL_ENGINE.setFunctions(ImmutableMap.<String, Object>of(
                //"fish", GreyfishVariableFactory.class,
                "math", Math.class,
                "rand", RandomGenerators.class));
    }

    private static final ImmutableMap<String, Object> GLOBAL_VARIABLES = ImmutableMap.<String, Object>builder()
            .put("PI", FastMath.PI)
            .put("HALF_PI", FastMath.PI / 2)
            .put("PI_SQUARE", FastMath.pow(FastMath.PI, 2))
            .put("TWO_PI", FastMath.PI * 2)
            .put("FOUR_PI", FastMath.PI * 4)
            .put("E", FastMath.exp(1))
            .build();

    private final Expression jexlCompiledExpression;

    public CommonsJEXLEvaluator(final String expression) {
        checkNotNull(expression);
        this.jexlCompiledExpression = JEXL_ENGINE.createExpression(prepare(expression));
        assert expression != null;
    }

    @Override
    public EvaluationResult evaluate(final VariableResolver resolver) {
        checkNotNull(resolver);
        final Object result = jexlCompiledExpression.evaluate(addGlobalVariables(resolver));
        return new ConvertingEvaluationResult(result);
    }

    @Override
    public String getExpression() {
        return jexlCompiledExpression.getExpression();
    }

    private static String prepare(final String expression) {
        // This will lift some function in the global namespace and allows users to write 'fun' instead of 'ns:fun'.
        return expression
                //.replaceAll("\\$\\(([^\\)]+)\\)", "fish:\\$($1)")
                .replaceAll("((rnorm|rpois|runif)\\([^\\)]+\\))", "rand:$1")
                .replaceAll("((min|max|abs|sin|cos|tan|log|log10)\\([^\\)]+\\))", "math:$1");
    }

    private static JEXLResolverAdaptor addGlobalVariables(final VariableResolver resolver) {
        assert resolver != null;

        final VariableResolver variableResolver = VariableResolvers.forMap(GLOBAL_VARIABLES);
        variableResolver.append(resolver);

        return new JEXLResolverAdaptor(variableResolver);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(CommonsJEXLEvaluator.class)
                .addValue(jexlCompiledExpression)
                .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final CommonsJEXLEvaluator that = (CommonsJEXLEvaluator) o;

        if (!jexlCompiledExpression.getExpression().equals(that.jexlCompiledExpression.getExpression())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return jexlCompiledExpression.getExpression().hashCode();
    }

    private static class SerializedForm implements Serializable {
        final String expression;

        SerializedForm(final String expression) {
            this.expression = expression;
        }

        Object readResolve() {
            return new CommonsJEXLEvaluator(expression);
        }

        private static final long serialVersionUID = 0;
    }

    Object writeReplace() {
        return new SerializedForm(jexlCompiledExpression == null ? null : jexlCompiledExpression.getExpression());
    }

    private static class JEXLResolverAdaptor extends ForwardingVariableResolver implements JexlContext, Serializable {
        private final VariableResolver resolver;

        public JEXLResolverAdaptor(final VariableResolver resolver) {
            this.resolver = resolver;
        }

        @Override
        protected VariableResolver delegate() {
            return resolver;
        }

        @Override
        public Object get(final String s) {
            return resolve(s);
        }

        @Override
        public void set(final String s, final Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean has(final String s) {
            return canResolve(s);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final JEXLResolverAdaptor that = (JEXLResolverAdaptor) o;

            if (!resolver.equals(that.resolver)) {
                return false;
            }

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
