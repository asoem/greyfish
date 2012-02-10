package org.asoem.greyfish.core.eval;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 13.09.11
 * Time: 14:09
 */
public class GreyfishExpression {

    private static final Pattern DOLLAR_FUNCTION_PATTERN = Pattern.compile("\\$\\(([^\\)]+)\\)");

    private final Evaluator evaluator;
    private final String expression;

    @Inject
    public GreyfishExpression(@Assisted String expression, Evaluator evaluator) {
        this.evaluator = checkNotNull(evaluator);
        this.expression = checkNotNull(expression);
        this.evaluator.setExpression(parameterizeDollarFunction(expression));
    }

    public double evaluateAsDouble(Object context) throws EvaluationException {
        checkNotNull(context, "Context must not be null");
        return evaluateAsDouble(createContextResolver(context));
    }

    public double evaluateAsDouble(Object context, String n1, Object v1) throws EvaluationException {
        checkNotNull(context, "Context must not be null");
        VariableResolver contextResolver = createContextResolver(context);
        VariableResolver resolver = VariableResolvers.forMap(ImmutableMap.of(n1, v1));
        contextResolver.setNext(resolver);
        return evaluateAsDouble(resolver);
    }

    public double evaluateAsDouble(Object context, Map<String, ?> localVariables) throws EvaluationException {
        checkNotNull(context, "Context must not be null");
        VariableResolver contextResolver = createContextResolver(context);
        VariableResolver resolver = VariableResolvers.forMap(localVariables);
        contextResolver.setNext(resolver);
        return evaluateAsDouble(resolver);
    }
    
    public boolean evaluateAsBoolean(Object context) throws EvaluationException {
        checkNotNull(context, "Context must not be null");
        return evaluateAsBoolean(createContextResolver(context));
    }

    private boolean evaluateAsBoolean(VariableResolver resolver) {
        evaluator.setResolver(resolver);
        return evaluator.evaluateAsBoolean();
    }

    private double evaluateAsDouble(VariableResolver resolver) {
        evaluator.setResolver(resolver);
        return evaluator.evaluateAsDouble();
    }

    private static String parameterizeDollarFunction(String expression) {
        return DOLLAR_FUNCTION_PATTERN.matcher(expression).replaceAll("\\$($1, _ctx_)");
    }

    public String getExpression() {
        return expression;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public VariableResolver createContextResolver(@Nullable Object ctx) {
        return VariableResolvers.forMap(ImmutableMap.of("_ctx_", ctx));
    }

    @Override
    public String toString() {
        return getExpression();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GreyfishExpression that = (GreyfishExpression) o;

        return evaluator.equals(that.evaluator) && expression.equals(that.expression);

    }

    @Override
    public int hashCode() {
        int result = evaluator.hashCode();
        result = 31 * result + expression.hashCode();
        return result;
    }
}