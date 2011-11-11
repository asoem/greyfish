package org.asoem.greyfish.core.eval;

import com.google.common.collect.ImmutableMap;
import org.asoem.greyfish.core.individual.AgentComponent;

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 13.09.11
 * Time: 14:09
 */
public class GreyfishExpression {

    private final Evaluator evaluator;
    private static final Pattern DOLLAR_FUNCTION_PATTERN = Pattern.compile("\\$\\(([^\\)]+)\\)");

    public GreyfishExpression(String expression, Evaluator evaluator) {
        this.evaluator = checkNotNull(evaluator);
        setExpression(expression);
    }

    public double evaluateAsDouble(AgentComponent context) throws EvaluationException {
        return evaluateAsDouble(createContextResolver(context));
    }

    public double evaluateAsDouble(AgentComponent context, String n1, Object v1) throws EvaluationException {
        VariableResolver contextResolver = createContextResolver(context);
        VariableResolver resolver = VariableResolvers.forMap(ImmutableMap.of(n1, v1));
        contextResolver.setNext(resolver);
        return evaluateAsDouble(resolver);
    }

    public boolean evaluateAsBoolean(AgentComponent context) throws EvaluationException {
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

    public void setExpression(String expression) {
        evaluator.setExpression(parameterizeDollarFunction(expression));
    }

    private static String parameterizeDollarFunction(String expression) {
        return DOLLAR_FUNCTION_PATTERN.matcher(expression).replaceAll("\\$($1, _ctx_)");
    }

    public String getExpression() {
        return evaluator.getExpression();
    }

    public VariableResolver createContextResolver(AgentComponent ctx) {
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

        return evaluator.equals(that.evaluator);

    }

    @Override
    public int hashCode() {
        return evaluator.hashCode();
    }
}